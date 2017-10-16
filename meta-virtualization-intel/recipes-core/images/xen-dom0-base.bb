SUMMARY = "The base system image which service domUs and dom0 derive from."

LICENSE = "MIT"

IMAGE_ROOTFS_SIZE ?= "65536"
IMAGE_OVERHEAD_FACTOR = "1.25"

# Don't include a rootfs in the hddimg files
ROOTFS = ""

IMAGE_FEATURES += "empty-root-password debug-tweaks"

# What to install
DOMAIN_INSTALL = "\
    busybox \
    busybox-mdev \
    base-passwd \
    e2fsprogs \
    packagegroup-core-boot \
    ${CORE_IMAGE_EXTRA_INSTALL} \
    "

# Don't need locale bits
IMAGE_LINGUAS = " "
PACKAGE_NO_LOCALE = "1"
LOCALE_UTF8_ONLY = "1"
LIMIT_BUILT_LOCALES = "posix"
FORCE_RO_REMOVE = "1"

ROOTFS_POSTPROCESS_COMMAND += "remove_update_alternatives ; enable_getty_support; "
remove_update_alternatives () {
    rm -rf ${IMAGE_ROOTFS}${libdir}/opkg
}

enable_getty_support () {
    echo "1:12345:respawn:/sbin/getty 38400 tty1" >> ${IMAGE_ROOTFS}/etc/inittab
    echo "X0:12345:respawn:/sbin/getty 115200 hvc0" >> ${IMAGE_ROOTFS}/etc/inittab
}

BAD_RECOMMENDATIONS += "busybox-syslog"
