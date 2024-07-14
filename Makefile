generate-examples:
	for generator in php php-nextgen java python ; do \
        docker run --rm -v "${PWD}:/local" openapitools/openapi-generator-cli generate -i /local/manifests/nullable-and-required.yaml -g $$generator -o /local/generated/$$generator/nullable-and-required ; \
    done

regenerate: compile-generator run-generator

compile-generator:
	docker-compose run -w /local/generators/php-custom maven mvn package

run-generator:
	docker-compose run openapi-generator-cli java \
		-cp /local/generators/php-custom/target/php-custom-openapi-generator-1.0.0.jar:/opt/openapi-generator/modules/openapi-generator-cli/target/openapi-generator-cli.jar \
		org.openapitools.codegen.OpenAPIGenerator generate \
        -i /local/manifests/books.yaml \
        -g php-custom \
        -o /local/generated/php-custom/books

create-generator:
	docker-compose run openapi-generator-cli meta -o /local/generators/php-custom -n php-custom -p com.yui.ezic.codegen
