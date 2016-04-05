#!/bin/bash

if [ $# -ne 2  ]; then
 echo "incorrect usage"
 exit 1
fi

#Variables
PROJECTID="$1"
USERID="$2"

#1 Create a user key
cd /root/ca
openssl genrsa \
      -out intermediate/private/${PROJECTID}__${USERID}.key.pem 2048

chmod 400 intermediate/private/${PROJECTID}__${USERID}.key.pem

#2 Create a user certificate
openssl req -config intermediate/openssl.cnf \
	  -subj "/C=SE/ST=Sweden/L=Stockholm/O=SICS/CN=$PROJECTID"__"$USERID" \
	  -passin pass:adminpw -passout pass:adminpw \
      -key intermediate/private/${PROJECTID}__${USERID}.key.pem \
      -new -sha256 -out intermediate/csr/${PROJECTID}__${USERID}.csr.pem

openssl ca -batch -config intermediate/openssl.cnf \
      -passin pass:adminpw \
      -extensions usr_cert -days 375 -notext -md sha256 \
      -in intermediate/csr/${PROJECTID}__${USERID}.csr.pem \
      -out intermediate/certs/${PROJECTID}__${USERID}.cert.pem

chmod 444 intermediate/certs/${PROJECTID}__${USERID}.cert.pem

#3 Verify the intermediate certificate
openssl verify -CAfile intermediate/certs/ca-chain.cert.pem \
      intermediate/certs/${PROJECTID}__${USERID}.cert.pem
