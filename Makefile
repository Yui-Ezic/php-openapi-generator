generate-examples:
	for generator in php php-nextgen java python ; do \
        docker run --rm -v "${PWD}:/local" openapitools/openapi-generator-cli generate \
			-i /local/manifests/nullable-and-required.yaml \
			-g $$generator \
			-o /local/generated/$$generator/nullable-and-required ; \
		docker run --rm -v "${PWD}:/local" openapitools/openapi-generator-cli generate \
			-i /local/manifests/books.yaml \
			-g $$generator \
			-o /local/generated/$$generator/books ; \
    done

regenerate: compile-generator clear-examples run-generator
regenerate-debug: compile-generator clear-examples run-generator-debug

compile-generator:
	docker-compose run -w /local/generators/php-custom maven mvn package

clear-examples:
	docker-compose run openapi-generator-cli rm -rf /local/examples/*

run-generator:
	docker-compose run openapi-generator-cli java \
		-cp /local/generators/php-custom/target/php-custom-openapi-generator-1.0.0.jar:/opt/openapi-generator/modules/openapi-generator-cli/target/openapi-generator-cli.jar \
		org.openapitools.codegen.OpenAPIGenerator generate \
        -i /local/manifests/books.yaml \
        -g php-custom \
        -o /local/examples/books

run-generator-debug:
	docker-compose run openapi-generator-cli java \
		-cp /local/generators/php-custom/target/php-custom-openapi-generator-1.0.0.jar:/opt/openapi-generator/modules/openapi-generator-cli/target/openapi-generator-cli.jar \
		org.openapitools.codegen.OpenAPIGenerator generate \
        -i /local/manifests/books.yaml \
        -g php-custom \
        -o /local/examples/books \
        --global-property debugModels,debugOperations

create-generator:
	docker-compose run openapi-generator-cli meta -o /local/generators/php-custom -n php-custom -p com.yui.ezic.codegen
