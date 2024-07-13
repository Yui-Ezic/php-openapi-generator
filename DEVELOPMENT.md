## Generate code

```bash 
docker run --rm -v "${PWD}:/local" openapitools/openapi-generator-cli generate -i /local/manifests/nullable-and-required.yaml -g php -o /local/generated/php/nullable-and-required
```

To generate all examples run

```bash
make generate
```