SERIAL_CONSOLES = "" 
SYSVINIT_ENABLED_GETTYS = ""

# Remove any lines containint getty that were put there by a bbappend
do_install_append() {
    sed -i '/\/sbin\/getty/d' ${D}${sysconfdir}/inittab
}
