# Add a copy for the multilib header
# Without this header existing, the 32-bit HVM code fails to compile
do_install_append_x86-64() {
    cp ${D}/${includedir}/bits/long-double-64.h ${D}/${includedir}/bits/long-double-32.h
}
