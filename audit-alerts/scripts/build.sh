IMAGE_REPOSITORY=${IMAGE_REPOSITORY:-quay.io/evanshortiss/shipwars-email-alerts:latest}

s2i build -c . --exclude "(^|/)\.git|.env*|node_modules|some_other_folder(/|$)" registry.access.redhat.com/ubi8/nodejs-14 $IMAGE_REPOSITORY
