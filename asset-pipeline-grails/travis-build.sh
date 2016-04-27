#!/bin/bash
set -e
rm -rf *.zip
./gradlew test assemble

filename=$(find build/libs -name "*.jar" | head -1)
filename=$(basename $filename)

if [[ $TRAVIS_PULL_REQUEST == 'false' ]]; then
    if [[ -n $TRAVIS_TAG ]]; then
        ./gradlew bintrayUpload || EXIT_STATUS=$?
    else
        ./gradlew publish || EXIT_STATUS=$?
    fi  

    ./gradlew docs || EXIT_STATUS=$?
  git config --global user.name "$GIT_NAME"
  git config --global user.email "$GIT_EMAIL"
  git config --global credential.helper "store --file=~/.git-credentials"
  echo "https://$GH_TOKEN:@github.com" > ~/.git-credentials

  if [[ $filename != *-SNAPSHOT* ]]
  then
    git clone https://${GH_TOKEN}@github.com/$TRAVIS_REPO_SLUG.git -b gh-pages gh-pages --single-branch > /dev/null
    cd gh-pages
    git rm -rf .
    cp -r ../build/docs/manual/. ./
    git add *
    git commit -a -m "Updating docs for Travis build: https://travis-ci.org/$TRAVIS_REPO_SLUG/builds/$TRAVIS_BUILD_ID"
    git push origin HEAD
    cd ..
    rm -rf gh-pages
  else
    echo "SNAPSHOT version, not publishing docs"
  fi
fi
