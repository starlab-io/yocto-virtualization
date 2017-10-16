SUMMARY = "The initramfs for Xen dom0"

IMAGE_FEATURES = ""

require xen-dom0-base.bb

# What to install
DOMAIN_INSTALL_append = "\
    ${@bb.utils.contains('MACHINE_FEATURES', 'acpi', 'kernel-module-xen-acpi-processor', '', d)} \
    kernel-module-xen-gntalloc \
    kernel-module-xen-gntdev \
    ${@bb.utils.contains('MACHINE_FEATURES', 'pci', 'kernel-module-xen-pciback', '', d)} \
    kernel-module-xen-wdt \
    xen-startvms \
    xen-base \
    xen-hvmloader \
    xen-devd \
    xen-xendomains \
    xen-scripts-block \
    xen-scripts-network \
    ${VIRTUAL-RUNTIME_base-utils-hwclock} \
    qemu \
    packagegroup-core-ssh-dropbear \
    "

DOMAIN_INSTALL_append_intel-corei7-64 = "\
    kernel-module-e1000 \
    kernel-module-e1000e \
    kernel-module-igb \
    kernel-module-igbvf \
    kernel-module-ixgb \
    kernel-module-ixgbe \
    kernel-module-ixgbevf \
  "

PACKAGE_INSTALL = "\
    ${DOMAIN_INSTALL} \
    "

IMAGE_FSTYPES = "${INITRAMFS_FSTYPES}"
inherit core-image

ROOTFS_POSTPROCESS_COMMAND += "set_dom0_hostname;"

set_dom0_hostname () {
    echo "dom0" > ${IMAGE_ROOTFS}/etc/hostname
}
