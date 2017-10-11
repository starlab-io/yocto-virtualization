FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

do_install_append () {
    find ${D} -name '*mountnfs*'  | xargs rm -f
    find ${D} -name '*umountnfs*' | xargs rm -f
    find ${D} -type f -name '*urandom*' | xargs rm -f
    find ${D} -type l -name '*urandom*' | xargs rm -f
}
