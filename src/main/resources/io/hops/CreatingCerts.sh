#!/bin/bash

if [ $# -ne 1  ]; then
 echo "incorrect usage"
 exit 1
fi

#Variables
DOMAIN="$1"
KEYSTOREPW=adminpw

#1 Create a user key
cd /srv/glassfish/domain1/config/ca/intermediate/
openssl genrsa \
      -out private/${DOMAIN}.key.pem 2048

chmod 400 private/${DOMAIN}.key.pem

#2 Create a user certificate
openssl req -config openssl.cnf \
	  -subj "/C=SE/ST=Sweden/L=Stockholm/O=SICS/CN=${DOMAIN}" \
	  -passin pass:$KEYSTOREPW -passout pass:$KEYSTOREPW \
      -key private/${DOMAIN}.key.pem \
      -new -sha256 -out csr/${DOMAIN}.csr.pem

openssl ca -batch -config openssl.cnf \
      -passin pass:$KEYSTOREPW \
      -extensions usr_cert -days 365 -notext -md sha256 \
      -in csr/${DOMAIN}.csr.pem \
      -out certs/${DOMAIN}.cert.pem

chmod 444 certs/${DOMAIN}.cert.pem

#3 Verify the intermediate certificate
## openssl verify -CAfile certs/ca-chain.cert.pem certs/${DOMAIN}.cert.pem

#Create new Keystore 
openssl pkcs12 -export -in certs/${DOMAIN}.cert.pem -inkey private/${DOMAIN}.key.pem -out cert_and_key.p12 -name $DOMAIN -CAfile certs/intermediate.cert.pem -caname root -password pass:$KEYSTOREPW
keytool -importkeystore -destkeystore ${DOMAIN}__kstore.jks -srckeystore cert_and_key.p12 -srcstoretype PKCS12 -alias $DOMAIN -srcstorepass $KEYSTOREPW -deststorepass $KEYSTOREPW -destkeypass $KEYSTOREPW
keytool -import -noprompt -trustcacerts -alias CARoot -file certs/intermediate.cert.pem -keystore ${DOMAIN}__kstore.jks -srcstorepass $KEYSTOREPW -deststorepass $KEYSTOREPW -destkeypass $KEYSTOREPW
keytool -import -noprompt -trustcacerts -alias CARoot -file certs/intermediate.cert.pem -keystore ${DOMAIN}__tstore.jks -srcstorepass $KEYSTOREPW -deststorepass $KEYSTOREPW -destkeypass $KEYSTOREPW

rm cert_and_key.p12