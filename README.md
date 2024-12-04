# Template for Scala JVM projects

This template is a starting point for a Scala JVM project for PostionPal organition.

## Supported libs
- Scala 3.x
- Scala Test

## Additional support
- Scala extras plugin for additional support using ScalaFmt and ScalaFix

## Github Action support
- Semantic release
- Automatic deploy on dockerhub
- Automatic repository synchronization

### On Docker support for the following repository

This templates enables automatic deployment on Dockerhub using GitHub actions alongside semantic release. Make sure that
`DOCKERHUB_USERNAME` and `DOCKERHUB_TOKEN` are available on the repo's secrets.

Usage on `.github/workflows/docker.yml`
```
        uses: docker/login-action@v3
        with:
          username: ${{ secrets.DOCKERHUB_USERNAME }}
          password: ${{ secrets.DOCKERHUB_TOKEN }}
```
