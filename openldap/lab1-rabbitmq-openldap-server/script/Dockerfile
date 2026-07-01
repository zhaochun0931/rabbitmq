FROM ubuntu:24.04

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && \
    apt-get install -y slapd ldap-utils && \
    apt-get clean

# Preconfigure slapd with domain and admin password
RUN echo "slapd slapd/internal/generated_adminpw password password" | debconf-set-selections && \
    echo "slapd slapd/internal/adminpw password password" | debconf-set-selections && \
    echo "slapd slapd/password2 password password" | debconf-set-selections && \
    echo "slapd slapd/password1 password password" | debconf-set-selections && \
    echo "slapd slapd/domain string example.com" | debconf-set-selections && \
    echo "slapd shared/organization string example" | debconf-set-selections && \
    dpkg-reconfigure -f noninteractive slapd

# init script
RUN echo '#!/bin/bash\n\
set -e\n\
echo "[+] Starting slapd..."\n\
/usr/sbin/slapd -u openldap -g openldap -h "ldap:/// ldapi:///"\n\
sleep 2\n\
echo "[+] Applying bootstrap LDIF..."\n\
ldapadd -x -D "cn=admin,dc=example,dc=com" -w password -f /bootstrap.ldif\n\
echo "[+] Done. slapd is running."\n\
tail -f /dev/null' > /init.sh

RUN chmod +x /init.sh

