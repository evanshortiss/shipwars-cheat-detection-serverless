IMAGE_TAG=${IMAGE_TAG:-latest}
IMAGE_REPOSITORY=${IMAGE_REPOSITORY:-quay.io/evanshortiss/shipwars-cheat-detection}

rm -rf node_modules/
rm -rf build/

if ! command -v podman &> /dev/null
then
    echo "use s2i"
    s2i build -c . -e HUSKY_SKIP_HOOKS=1 registry.access.redhat.com/ubi8/nodejs-14 ${IMAGE_REPOSITORY}:${IMAGE_TAG}
else
    echo "use podman"
    rm -rf /tmp/upload
    s2i build -e HUSKY_SKIP_HOOKS=1 -c . --as-dockerfile /tmp/Dockerfile.generated registry.access.redhat.com/ubi8/nodejs-14 
    podman build /tmp -f /tmp/Dockerfile.generated -t ${IMAGE_REPOSITORY}:${IMAGE_TAG}
fi
