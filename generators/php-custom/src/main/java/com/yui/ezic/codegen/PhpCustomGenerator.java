package com.yui.ezic.codegen;

import io.swagger.v3.oas.models.media.Schema;
import org.apache.commons.lang3.StringUtils;
import org.openapitools.codegen.*;
import org.openapitools.codegen.languages.AbstractPhpCodegen;
import org.openapitools.codegen.meta.GeneratorMetadata;
import org.openapitools.codegen.meta.Stability;
import org.openapitools.codegen.meta.features.*;
import org.openapitools.codegen.model.ModelMap;
import org.openapitools.codegen.model.ModelsMap;
import org.openapitools.codegen.model.OperationMap;
import org.openapitools.codegen.model.OperationsMap;
import org.openapitools.codegen.utils.ModelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

public class PhpCustomGenerator extends AbstractPhpCodegen {
    @SuppressWarnings("hiding")
    private final Logger LOGGER = LoggerFactory.getLogger(PhpCustomGenerator.class);

    public PhpCustomGenerator() {
        super();

        // override default src and test folders to comply PSD skeleton
        setSrcBasePath("src");
        setTestBasePath("tests");

        // mark as beta so far
        this.generatorMetadata = GeneratorMetadata.newBuilder(generatorMetadata)
                .stability(Stability.BETA).build();

        modifyFeatureSet(features -> features
                .includeDocumentationFeatures(DocumentationFeature.Readme)
                .wireFormatFeatures(EnumSet.of(WireFormatFeature.JSON, WireFormatFeature.XML))
                .securityFeatures(EnumSet.noneOf(SecurityFeature.class))
                .excludeGlobalFeatures(
                        GlobalFeature.XMLStructureDefinitions,
                        GlobalFeature.Callbacks,
                        GlobalFeature.LinkObjects,
                        GlobalFeature.ParameterStyling
                )
                .excludeSchemaSupportFeatures(
                        SchemaSupportFeature.Polymorphism
                )
        );

        // clear import mapping (from default generator) as php does not use it
        // at the moment
        importMapping.clear();

        setInvokerPackage("OpenAPI\\Client");
        setApiPackage(getInvokerPackage() + "\\" + apiDirName);
        setModelPackage(getInvokerPackage() + "\\" + modelDirName);
        setPackageName("OpenAPIClient-php");
        supportsInheritance = true;
        setOutputDir("generated-code" + File.separator + "php");
        modelTestTemplateFiles.put("model_test.mustache", ".php");
        embeddedTemplateDir = templateDir = "php-custom";

        // default HIDE_GENERATION_TIMESTAMP to true
        hideGenerationTimestamp = Boolean.TRUE;

        // provide primitives to mustache template
        List sortedLanguageSpecificPrimitives = new ArrayList(languageSpecificPrimitives);
        Collections.sort(sortedLanguageSpecificPrimitives);
        String primitives = "'" + StringUtils.join(sortedLanguageSpecificPrimitives, "', '") + "'";
        additionalProperties.put("primitives", primitives);

        cliOptions.add(new CliOption(CodegenConstants.HIDE_GENERATION_TIMESTAMP, CodegenConstants.ALLOW_UNICODE_IDENTIFIERS_DESC)
                .defaultValue(Boolean.TRUE.toString()));
    }

    @Override
    public CodegenType getTag() {
        return CodegenType.CLIENT;
    }

    @Override
    public String getName() {
        return "php-custom";
    }

    @Override
    public String getHelp() {
        return "Generates a PHP client library (beta).";
    }

    @Override
    public void processOpts() {
        super.processOpts();

        supportingFiles.add(new SupportingFile("ApiException.mustache", toSrcPath(invokerPackage, srcBasePath), "ApiException.php"));
        supportingFiles.add(new SupportingFile("Configuration.mustache", toSrcPath(invokerPackage, srcBasePath), "Configuration.php"));
        supportingFiles.add(new SupportingFile("ObjectSerializer.mustache", toSrcPath(invokerPackage, srcBasePath), "ObjectSerializer.php"));
        supportingFiles.add(new SupportingFile("ModelInterface.mustache", toSrcPath(modelPackage, srcBasePath), "ModelInterface.php"));
        supportingFiles.add(new SupportingFile("HeaderSelector.mustache", toSrcPath(invokerPackage, srcBasePath), "HeaderSelector.php"));
        supportingFiles.add(new SupportingFile("composer.mustache", "", "composer.json"));
        supportingFiles.add(new SupportingFile("README.mustache", "", "README.md"));
        supportingFiles.add(new SupportingFile("phpunit.xml.mustache", "", "phpunit.xml.dist"));
        supportingFiles.add(new SupportingFile("git_push.sh.mustache", "", "git_push.sh"));
    }

    @Override
    public Map<String, ModelsMap> postProcessAllModels(Map<String, ModelsMap> objs) {
        final Map<String, ModelsMap> processed = super.postProcessAllModels(objs);

        for (Map.Entry<String, ModelsMap> entry : processed.entrySet()) {
            entry.setValue(postProcessModelsMap(entry.getValue()));
        }

        return processed;
    }

    private ModelsMap postProcessModelsMap(ModelsMap objs) {
        for (ModelMap m : objs.getModels()) {
            CodegenModel model = m.getModel();

            for (CodegenProperty prop : model.vars) {
                String propType;
                if (prop.isArray || prop.isMap) {
                    propType = "array";
                } else {
                    propType = prop.dataType;
                }

                if ((!prop.required || prop.isNullable) && !propType.equals("mixed")) { // optional or nullable but not mixed
                    propType = "?" + propType;
                }

                prop.vendorExtensions.putIfAbsent("x-php-prop-type", propType);

                // Resolve psalm type
                String psalmType;
                switch (prop.openApiType) {
                    case "string":
                        if (prop.dataFormat != null) {
                            switch (prop.dataFormat) {
                                case "date":
                                case "dateTime":
                                    psalmType = "\\DateTime";
                                    break;
                                default:
                                    psalmType = null;
                                    break;
                            }
                        } else {
                            psalmType = null;
                        }

                        if (psalmType == null) {
                            psalmType = prop.minLength != null && prop.minLength > 0 ? "non-empty-string" : "string";
                        }
                        break;
                    case "number":
                        psalmType = "float";
                        break;
                    case "integer":
                        String minimum = prop.minimum == null ? "min" : prop.exclusiveMinimum ? prop.minimum + 1 : prop.minimum;
                        String maximum = prop.maximum == null ? "max" : prop.exclusiveMaximum ? prop.maximum + 1 : prop.maximum;
                        psalmType = "int<" + minimum + ", " + maximum + ">";
                        break;
                    case "boolean":
                        psalmType = "bool";
                        break;
                    case "array":
                        psalmType = "array";
                        break;
                    case "object":
                        psalmType = "object";
                        break;
                    default:
                        psalmType = null;
                        break;
                }

                if (psalmType != null && prop.isNullable) {
                    psalmType += "|null";
                }

                prop.vendorExtensions.putIfAbsent("x-php-psalm-type", psalmType);
            }

            if (model.isEnum) {
                for (Map<String, Object> enumVars : (List<Map<String, Object>>) model.getAllowableValues().get("enumVars")) {
                    if ((Boolean) enumVars.get("isString")) {
                        model.vendorExtensions.putIfAbsent("x-php-enum-type", "string");
                    } else {
                        model.vendorExtensions.putIfAbsent("x-php-enum-type", "int");
                    }
                }
            }
        }
        return objs;
    }

    @Override
    public OperationsMap postProcessOperationsWithModels(OperationsMap objs, List<ModelMap> allModels) {
        objs = super.postProcessOperationsWithModels(objs, allModels);
        OperationMap operations = objs.getOperations();
        for (CodegenOperation operation : operations.getOperation()) {
            if (operation.returnType == null) {
                operation.vendorExtensions.putIfAbsent("x-php-return-type", "void");
            } else {
                if (operation.returnProperty.isContainer) { // array or map
                    operation.vendorExtensions.putIfAbsent("x-php-return-type", "array");
                } else {
                    operation.vendorExtensions.putIfAbsent("x-php-return-type", operation.returnType);
                }
            }

            for (CodegenParameter param : operation.allParams) {
                if (param.isArray || param.isMap) {
                    param.vendorExtensions.putIfAbsent("x-php-param-type", "array");
                } else {
                    param.vendorExtensions.putIfAbsent("x-php-param-type", param.dataType);
                }
            }
        }

        return objs;
    }

    @Override
    public String toDefaultValue(CodegenProperty codegenProperty, Schema schema) {

        if (codegenProperty.isArray) {
            schema = ModelUtils.getReferencedSchema(this.openAPI, schema);

            if (schema.getDefault() != null) { // array schema has default value
                return "[" + schema.getDefault().toString() + "]";
            } else if (schema.getItems().getDefault() != null) { // array item schema has default value
                return "[" + toDefaultValue(schema.getItems()) + "]";
            } else {
                return null;
            }
        }
        return super.toDefaultValue(codegenProperty, schema);
    }

    @Override
    public String toDefaultParameterValue(CodegenProperty codegenProperty, Schema<?> schema) {
        return toDefaultValue(codegenProperty, schema);
    }

    @Override
    public void setParameterExampleValue(CodegenParameter p) {
        if (p.isArray && p.items.defaultValue != null) {
            p.example = p.defaultValue;
        } else {
            super.setParameterExampleValue(p);
        }
    }
}
