#! /bin/bash

#! /bin/bash

set -e -u -o pipefail

HELP="
Usage: $0 [options]

  (--help -h) Display this message.
  * (--domain -d HELLO_SHADOW_AUTH_DOMAIN)
  * (--client-id -c HELLO_SHADOW_AUTH_CLIENT_ID)
  * (--audience -a HELLO_SHADOW_AUTH_AUDIENCE)

	* REQUIRED
"

while [ $# -gt 0 ]; do
	case "$1" in
	"-h"|"--help")
		echo "$HELP"
		exit 0
		;;
	"--domain"|"-d")
		HELLO_SHADOW_AUTH_DOMAIN="$2"
		shift 2
		;;
  "--client-id"|"-c")
		HELLO_SHADOW_AUTH_CLIENT_ID="$2"
		shift 2
    ;;
  "--audience"|"-a")
		HELLO_SHADOW_AUTH_AUDIENCE="$2"
		shift 2
    ;;
	*)
		echo "Unrecognized option: $1"
		echo "$HELP"
		exit 1
		;;
	esac
done

DESTFILE=scripts/shadow-env-exports

echo "" > "${DESTFILE}"
echo "export HELLO_SHADOW_AUTH_DOMAIN=${HELLO_SHADOW_AUTH_DOMAIN}" >> "${DESTFILE}"
echo "export HELLO_SHADOW_AUTH_CLIENT_ID=${HELLO_SHADOW_AUTH_CLIENT_ID}" >> "${DESTFILE}"
echo "export HELLO_SHADOW_AUTH_AUDIENCE=${HELLO_SHADOW_AUTH_AUDIENCE}" >> "${DESTFILE}"

# After running this, use `source "${DESTFILE}"` to load environment
