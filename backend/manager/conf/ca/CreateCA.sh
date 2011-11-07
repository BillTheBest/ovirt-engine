#!/bin/sh

die () {
        printf >&2 "$@"
        exit 1
}

usage () {
        printf "CreateCA.sh - Creates Certificate Authority certificate and keys\n"
        printf "USAGE:\n"
        printf "\tCreateCA [Country] [Organization] [Name] [startdate]\n"
        printf "Where:\n"
        printf "\tCountry      = 2 Letters country code\n"
        printf "\tOrganization = Organization name string\n"
        printf "\tName         = CA Subject Name\n"
        printf "\tstartdate    = in YYMMDDHHMMSSZ ASN1 format\n"
        return 0
}

if [ ! "$#" -eq 4 ]; then
	usage
	die "Error: wrong argument number: $#.\n"
fi

cp cacert.template cacert.conf
echo C = $1 >> cacert.conf
echo O = $2 >> cacert.conf
echo CN = $3 >> cacert.conf

openssl genrsa -out private/ca.pem 1024 && openssl req -new -key private/ca.pem -config cacert.conf -out requests/ca.csr && openssl ca -selfsign -out ca.pem -in requests/ca.csr -keyfile private/ca.pem -days 3650 -startdate $4 -config openssl.conf -extfile cacert.conf -extensions v3_ca -batch

exit $?

