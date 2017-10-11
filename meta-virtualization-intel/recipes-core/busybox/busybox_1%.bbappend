FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

SRC_URI_remove += " \
    file://lspci.cfg \
    file://lsusb.cfg \
    file://mdev.cfg \
    file://sha1sum.cfg \
    file://sha256sum.cfg \
    file://mount-cifs.cfg \
    file://ps-extras.cfg \
    file://getopt.cfg \
    file://login-utilities.cfg \
"

# This should not be required but SSTATE is being lame
PR_append = "-r3"

do_install_append() {
    mv ${D}/${sysconfdir}/mdev.conf ${D}/${sysconfdir}/mdev.conf.${BPN}
}

FILES_${PN}-mdev += "${sysconfdir}/mdev.conf.${BPN}"

ALTERNATIVE_${PN}-mdev = "mdev.conf"
ALTERNATIVE_TARGET[mdev.conf] = "${sysconfdir}/mdev.conf.${BPN}"
ALTERNATIVE_LINK_NAME[mdev.conf] = "${sysconfdir}/mdev.conf"
ALTERNATIVE_PRIORITY[mdev.conf] = "10"
