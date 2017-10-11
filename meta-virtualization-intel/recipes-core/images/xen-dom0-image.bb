DESCRIPTION = "Xen dom0 image"

INITRD_IMAGE_LIVE ?= "xen-dom0-initramfs"
INITRD_LIVE_FINAL ?= "${INITRD_IMAGE_LIVE}"

require xen-dom0-base.bb

IMAGE_INSTALL = " \
    ${DOMAIN_INSTALL} \
    ${@bb.utils.contains('MACHINE_FEATURES', 'acpi', 'kernel-module-xen-acpi-processor', '', d)} \
    kernel-module-xen-gntalloc \
    kernel-module-xen-gntdev \
    kernel-module-xen-netback \
    ${@bb.utils.contains('MACHINE_FEATURES', 'pci', 'kernel-module-xen-pciback', '', d)} \
    kernel-module-xen-wdt \
    xen-base \
    "

#IMAGE_INSTALL_append_intel-corei7-64 = "\
#    kernel-module-ixgbe \
#    kernel-module-igb \
#  "

inherit core-image

syslinux_iso_populate_append() {
    install -m 0444 ${STAGING_DATADIR}/syslinux/libcom32.c32 ${ISODIR}${ISOLINUXDIR}
    install -m 0444 ${STAGING_DATADIR}/syslinux/mboot.c32 ${ISODIR}${ISOLINUXDIR}
}

syslinux_hddimg_populate_append() {
    install -m 0444 ${STAGING_DATADIR}/syslinux/libcom32.c32 ${HDDDIR}${SYSLINUXDIR}
    install -m 0444 ${STAGING_DATADIR}/syslinux/mboot.c32 ${HDDDIR}${SYSLINUXDIR}
}

syslinux_populate_append() {
    install -m 0644 ${DEPLOY_DIR_IMAGE}/xen-${MACHINE}.gz ${DEST}/xen.gz
    gunzip -f ${DEST}/xen.gz
    install -m 0644 ${DEST}/initrd `readlink -f ${DEPLOY_DIR_IMAGE}/${INITRD_LIVE_FINAL}-${MACHINE}.cpio.gz`
}

SYSLINUX_XEN_ARGS ?= "loglvl=all guest_loglvl=all console=com1,vga com1=115200,8n1 iommu=force,intpost ucode=scan dom0pvh=0 dom0_mem=1024M,max:1024M dom0_max_vcpus=1 dom0_vcpus_pin flask=enforcing"
SYSLINUX_KERNEL_ARGS ?= "ramdisk_size=32768 root=/dev/ram0 rw console=hvc0 earlyprintk=xen console=tty0 panic=10 LABEL=boot debugshell=5"

build_syslinux_cfg () {
    echo "ALLOWOPTIONS 1" > ${SYSLINUX_CFG}
    echo "DEFAULT boot" >> ${SYSLINUX_CFG}
    echo "TIMEOUT 10" >> ${SYSLINUX_CFG}
    echo "PROMPT 1" >> ${SYSLINUX_CFG}
    echo "LABEL boot" >> ${SYSLINUX_CFG}
    echo "  KERNEL mboot.c32" >> ${SYSLINUX_CFG}
    echo "  APPEND /xen ${SYSLINUX_XEN_ARGS} --- /vmlinuz ${SYSLINUX_KERNEL_ARGS} --- /initrd" >> ${SYSLINUX_CFG}
}

build_efi_cfg () {
    echo "${GRUB_OPTS}" > ${GRUB_CFG}
    echo "default=boot" >> ${GRUB_CFG}
    echo "timeout=10" >> ${GRUB_CFG}
    echo "" >> ${GRUB_CFG}
    echo "menuentry 'boot' {" >> ${GRUB_CFG}
    echo "  echo 'Loading xen ...'" >> ${GRUB_CFG}
    echo "  multiboot /xen placeholder ${SYSLINUX_XEN_ARGS}" >> ${GRUB_CFG}
    echo "  echo 'Loading vmlinuz ...'" >> ${GRUB_CFG}
    echo "  module /vmlinuz placeholder ${SYSLINUX_KERNEL_ARGS}" >> ${GRUB_CFG}
    echo "  echo 'Loading initial ramdisk ...'" >> ${GRUB_CFG}
    echo "  module --nounzip /initrd" >> ${GRUB_CFG}
    echo "}" >>  ${GRUB_CFG}
}

do_bootimg[depends] += "parted-native:do_populate_sysroot \
                        ${INITRD_IMAGE_LIVE}:do_rootfs"

IMAGE_ALIGNMENT ?= "32"
BOOTIMG_VOLUME_ID   ?= "boot"

build_hddimg_append () {
    FATIMG_SIZE=$(stat -c%s ${IMGDEPLOYDIR}/${IMAGE_NAME}.hddimg)
    FATIMG_SIZE=$(expr ${FATIMG_SIZE} / 1024 + 1)
    FATIMG="${IMGDEPLOYDIR}/${IMAGE_NAME}-partitioned.hddimg"

    # Align partitions
    BOOT_SPACE_ALIGNED=$(expr ${FATIMG_SIZE} + ${IMAGE_ALIGNMENT} - 1)
    BOOT_SPACE_ALIGNED=$(expr ${BOOT_SPACE_ALIGNED} - ${BOOT_SPACE_ALIGNED} % ${IMAGE_ALIGNMENT})
    IMG_SIZE=$(expr ${IMAGE_ALIGNMENT} + ${BOOT_SPACE_ALIGNED} + ${IMAGE_ALIGNMENT})

    # Initialize sdcard image file
    dd if=/dev/zero of=${FATIMG} bs=1 count=0 seek=$(expr 1024 \* ${IMG_SIZE})

    # Create partition table
    parted -s ${FATIMG} mklabel msdos

    # Create boot partition and mark it as bootable
    parted -s ${FATIMG} unit KiB mkpart primary fat32 ${IMAGE_ALIGNMENT} $(expr ${BOOT_SPACE_ALIGNED} \+ ${IMAGE_ALIGNMENT})
    parted -s ${FATIMG} set 1 boot on

    # Create a vfat image with boot files
    BOOT_BLOCKS=$(LC_ALL=C parted -s ${FATIMG} unit b print | awk '/ 1 / { print substr($4, 1, length($4 -1)) / 512 /2 }')
    mkfs.vfat -n "${BOOT_VOLUME_ID}" -S 512 -C ${WORKDIR}/boot.img $BOOT_BLOCKS

    mkdir ${WORKDIR}/bootfiles
    mcopy -i ${IMGDEPLOYDIR}/${IMAGE_NAME}.hddimg -s ::/ ${WORKDIR}/bootfiles/.
    mcopy -i ${WORKDIR}/boot.img -s ${WORKDIR}/bootfiles/* ::/
    rm -rf ${WORKDIR}/bootfiles

    if [ "${PCBIOS}" = "1" ]; then
        syslinux ${WORKDIR}/boot.img
    fi

    dd if=${WORKDIR}/boot.img of=${FATIMG} conv=notrunc seek=1 bs=$(expr ${IMAGE_ALIGNMENT} \* 1024) && sync && sync

    ln -sf ${IMAGE_NAME}-partitioned.hddimg ${IMGDEPLOYDIR}/${IMAGE_LINK_NAME}-partitioned.hddimg

    rm -f ${WORKDIR}/boot.img
}
