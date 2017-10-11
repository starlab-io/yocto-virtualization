PACKAGES =+ "${PN}-unused"

ALTERNATIVE_${PN}-unused = "last lastb mesg utmpdump wall"

FILES_${PN}-unused = " \
    ${bindir}/last.* \
    ${bindir}/lastb.* \
    ${bindir}/mesg.* \
    ${base_bindir}/mountpoint.* \
    ${bindir}/utmpdump.* \
    ${bindir}/wall.* \
"
