generate:
	for generator in php php-nextgen java python ; do \
        docker run --rm -v "${PWD}:/local" openapitools/openapi-generator-cli generate -i /local/manifests/nullable-and-required.yaml -g $$generator -o /local/generated/$$generator/nullable-and-required ; \
    done