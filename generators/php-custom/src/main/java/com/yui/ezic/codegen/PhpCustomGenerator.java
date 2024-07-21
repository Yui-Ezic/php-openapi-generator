package com.yui.ezic.codegen;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.PathItem.HttpMethod;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.DateSchema;
import io.swagger.v3.oas.models.media.DateTimeSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PhpCustomGenerator extends AbstractPhpCodegen {
    @SuppressWarnings("hiding")
    private final Logger LOGGER = LoggerFactory.getLogger(PhpCustomGenerator.class);

    // Internal vendor extension names for extra template data that should not be set in specification
    public static final String VEN_PARAMETER_LOCATION = "internal.parameterLocation";
    public static final String VEN_FROM_PARAMETERS = "internal.fromParameters";
    public static final String VEN_COLLECTION_FORMAT = "internal.collectionFormat";
    public static final String VEN_PARAMETER_DATA_TYPE = "internal.parameterDataType";
    public static final String VEN_HAS_PARAMETER_DATA = "internal.hasParameterData";
    public static final String VEN_FROM_CONTAINER = "internal.fromContainer";
    public static final String VEN_CONTAINER_DATA_TYPE = "internal.containerDataType";

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

                Map<String, Object> resolvedPsalmType = resolvePsalmType(prop);
                for (Map.Entry<String, Object> entry : resolvedPsalmType.entrySet()) {
                    prop.vendorExtensions.putIfAbsent(entry.getKey(), entry.getValue());
                }
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

    private Map<String, Object> resolvePsalmType(CodegenProperty prop) {
        Map<String, Object> result = new HashMap<>();

        String psalmType;
        // Relative to Models directory
        String relativePsalmType;
        Boolean psalmTypeMoreSpecificThanNative;
        
        switch (prop.openApiType) {
            case "string":
                if (prop.dataFormat != null) {
                    switch (prop.dataFormat) {
                        case "date":
                        case "dateTime":
                            psalmType = "\\DateTime";
                            relativePsalmType = psalmType;
                            psalmTypeMoreSpecificThanNative = false;
                            break;
                        default:
                            psalmType = null;
                            relativePsalmType = null;
                            psalmTypeMoreSpecificThanNative = false;
                            break;
                    }
                } else {
                    psalmType = null;
                    relativePsalmType = null;
                    psalmTypeMoreSpecificThanNative = false;
                }

                if (psalmType == null) {
                    if (prop.minLength != null && prop.minLength > 0) {
                        psalmType = "non-empty-string";
                        relativePsalmType = psalmType;
                        psalmTypeMoreSpecificThanNative = true;
                    } else {
                        psalmType = "string";
                        relativePsalmType = psalmType;
                        psalmTypeMoreSpecificThanNative = false;
                    }
                }
                break;
            case "number":
                psalmType = "float";
                relativePsalmType = "float";
                psalmTypeMoreSpecificThanNative = false;
                break;
            case "integer":
                String minimum = prop.minimum == null ? "min" : prop.exclusiveMinimum ? prop.minimum + 1 : prop.minimum;
                String maximum = prop.maximum == null ? "max" : prop.exclusiveMaximum ? prop.maximum + 1 : prop.maximum;
                psalmType = "int<" + minimum + ", " + maximum + ">";
                relativePsalmType = psalmType;
                if (minimum == "min" && maximum == "max") {
                    psalmTypeMoreSpecificThanNative = false;
                } else {
                    psalmTypeMoreSpecificThanNative = true;
                }
                break;
            case "boolean":
                psalmType = "bool";
                relativePsalmType = psalmType;
                psalmTypeMoreSpecificThanNative = false;
                break;
            case "array":
                if (prop.items != null) {
                    Map<String, Object> resolvedPsalmType = resolvePsalmType(prop.items);
                    String itemsRelativePsalmType = (String)resolvedPsalmType.get("x-php-relative-psalm-type");
                    psalmType = "list<" + itemsRelativePsalmType + ">"; 
                    relativePsalmType = psalmType;
                    psalmTypeMoreSpecificThanNative = true;
                } else {
                    psalmType = "array";
                    relativePsalmType = psalmType;
                    psalmTypeMoreSpecificThanNative = false;
                }
                break;
            case "object":
                psalmType = "object";
                relativePsalmType = psalmType;
                psalmTypeMoreSpecificThanNative = false;
                break;
            default:
                psalmType = prop.dataType;
                relativePsalmType = prop.baseType;
                psalmTypeMoreSpecificThanNative = !prop.isModel;
                break;
        }

        if (psalmType != null && prop.isNullable) {
            psalmType += "|null";
        }

        result.put("x-php-psalm-type", psalmType);
        result.put("x-php-relative-psalm-type", relativePsalmType);
        result.put("x-php-psalm-type-more-specific-than-native", psalmTypeMoreSpecificThanNative);

        return result;
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

    // @Override
    public void preprocessOpenAPI(OpenAPI openAPI) {
        super.preprocessOpenAPI(openAPI);

        generateParameterSchemas(openAPI);
        generateContainerSchemas(openAPI);
    }

    /**
     * Generate additional model definitions from query parameters
     *
     * @param openAPI OpenAPI object
     */
    protected void generateParameterSchemas(OpenAPI openAPI) {
        Map<String, PathItem> paths = openAPI.getPaths();
        if (paths != null) {
            for (Map.Entry<String, PathItem> pathsEntry : paths.entrySet()) {
                String pathname = pathsEntry.getKey();
                PathItem path = pathsEntry.getValue();
                Map<HttpMethod, Operation> operationMap = path.readOperationsMap();
                if (operationMap != null) {
                    for (Map.Entry<HttpMethod, Operation> operationMapEntry : operationMap.entrySet()) {
                        HttpMethod method = operationMapEntry.getKey();
                        Operation operation = operationMapEntry.getValue();
                        Map<String, Schema> propertySchemas = new HashMap<>();
                        if (operation == null || operation.getParameters() == null) {
                            continue;
                        }

                        List<String> requiredProperties = new ArrayList<>();
                        for (Parameter parameter : operation.getParameters()) {
                            Parameter referencedParameter = ModelUtils.getReferencedParameter(openAPI, parameter);
                            Schema propertySchema = convertParameterToSchema(openAPI, referencedParameter);
                            if (propertySchema != null) {
                                propertySchemas.put(propertySchema.getName(), propertySchema);
                                if (Boolean.TRUE.equals(referencedParameter.getRequired())) {
                                    requiredProperties.add(propertySchema.getName());
                                }
                            }
                        }

                        if (!propertySchemas.isEmpty()) {
                            ObjectSchema schema = new ObjectSchema();
                            String operationId = getOrGenerateOperationId(operation, pathname, method.name());
                            schema.setDescription("Parameters for " + operationId);
                            schema.setProperties(propertySchemas);
                            schema.setRequired(requiredProperties);
                            addInternalExtensionToSchema(schema, VEN_FROM_PARAMETERS, Boolean.TRUE);
                            String schemaName = generateUniqueSchemaName(openAPI, operationId + "ParameterData");
                            openAPI.getComponents().addSchemas(schemaName, schema);
                            String schemaDataType = getTypeDeclaration(toModelName(schemaName));
                            addInternalExtensionToOperation(operation, VEN_PARAMETER_DATA_TYPE, schemaDataType);
                            addInternalExtensionToOperation(operation, VEN_HAS_PARAMETER_DATA, Boolean.TRUE);
                        }
                    }
                }
            }
        }
    }

    protected Schema convertParameterToSchema(OpenAPI openAPI, Parameter parameter) {
        Schema property = null;

        Schema parameterSchema = ModelUtils.getReferencedSchema(openAPI, parameter.getSchema());
        // array
        if (ModelUtils.isArraySchema(parameterSchema)) {
            Schema itemSchema = ModelUtils.getSchemaItems(parameterSchema);
            ArraySchema arraySchema = new ArraySchema();
            arraySchema.setMinItems(parameterSchema.getMinItems());
            arraySchema.setMaxItems(parameterSchema.getMaxItems());
            arraySchema.setItems(itemSchema);
            String collectionFormat = getCollectionFormat(parameter);
            if (collectionFormat == null) {
                collectionFormat = "csv";
            }
            addInternalExtensionToSchema(arraySchema, VEN_COLLECTION_FORMAT, collectionFormat);
            property = arraySchema;
        } else { // non-array e.g. string, integer
            switch (parameterSchema.getType()) {
                case "string":
                    StringSchema stringSchema = new StringSchema();
                    stringSchema.setMinLength(parameterSchema.getMinLength());
                    stringSchema.setMaxLength(parameterSchema.getMaxLength());
                    stringSchema.setPattern(parameterSchema.getPattern());
                    stringSchema.setEnum(parameterSchema.getEnum());
                    property = stringSchema;
                    break;
                case "integer":
                    IntegerSchema integerSchema = new IntegerSchema();
                    integerSchema.setMinimum(parameterSchema.getMinimum());
                    integerSchema.setMaximum(parameterSchema.getMaximum());
                    property = integerSchema;
                    break;
                case "number":
                    NumberSchema floatSchema = new NumberSchema();
                    floatSchema.setMinimum(parameterSchema.getMinimum());
                    floatSchema.setMaximum(parameterSchema.getMaximum());
                    property = floatSchema;
                    break;
                case "boolean":
                    property = new BooleanSchema();
                    break;
                case "date":
                    property = new DateSchema();
                    break;
                case "date-time":
                    property = new DateTimeSchema();
                    break;
            }
        }

        if (property != null) {
            property.setName(parameter.getName());
            property.setDescription(parameter.getDescription());
            addInternalExtensionToSchema(property, VEN_PARAMETER_LOCATION, parameter.getIn());
        }
        return property;
    }

    protected void addInternalExtensionToSchema(Schema schema, String name, Object value) {
        //Add internal extension directly, because addExtension filters extension names
        if (schema.getExtensions() == null) {
            schema.setExtensions(new HashMap<>());
        }
        schema.getExtensions().put(name, value);
    }

    protected void addInternalExtensionToOperation(Operation operation, String name, Object value) {
        //Add internal extension directly, because addExtension filters extension names
        if (operation.getExtensions() == null) {
            operation.setExtensions(new HashMap<>());
        }
        operation.getExtensions().put(name, value);
    }

    protected String generateUniqueSchemaName(OpenAPI openAPI, String name) {
        String result = name;
        if (openAPI.getComponents().getSchemas() != null) {
            int count = 1;
            while (openAPI.getComponents().getSchemas().containsKey(result)) {
                result = name + "_" + count;
                count += 1;
            }
        }
        return result;
    }

    /**
     * Generate additional model definitions for containers in whole specification
     *
     * @param openAPI OpenAPI object
     */
    protected void generateContainerSchemas(OpenAPI openAPI) {
        Set<Schema> visitedSchemas = new HashSet<>();
        Paths paths = openAPI.getPaths();
        for (String pathName : paths.keySet()) {
            for (Operation operation : paths.get(pathName).readOperations()) {
                List<Parameter> parameters = operation.getParameters();
                if (parameters != null) {
                    for (Parameter parameter : parameters) {
                        generateContainerSchemas(openAPI, visitedSchemas, ModelUtils.getReferencedParameter(openAPI, parameter).getSchema());
                    }
                }
                RequestBody requestBody = ModelUtils.getReferencedRequestBody(openAPI, operation.getRequestBody());
                if (requestBody != null) {
                    Content requestBodyContent = requestBody.getContent();
                    if (requestBodyContent != null) {
                        for (String mediaTypeName : requestBodyContent.keySet()) {
                            generateContainerSchemas(openAPI, visitedSchemas, requestBodyContent.get(mediaTypeName).getSchema());
                        }
                    }
                }
                ApiResponses responses = operation.getResponses();
                for (String responseCode : responses.keySet()) {
                    ApiResponse response = ModelUtils.getReferencedApiResponse(openAPI, responses.get(responseCode));
                    Content responseContent = response.getContent();
                    if (responseContent != null) {
                        for (String mediaTypeName : responseContent.keySet()) {
                            generateContainerSchemas(openAPI, visitedSchemas, responseContent.get(mediaTypeName).getSchema());
                        }
                    }
                }
            }
        }
    }

    /**
     * Generate additional model definitions for containers in specified schema
     *
     * @param openAPI OpenAPI object
     * @param visitedSchemas Set of Schemas that have been processed already
     * @param schema  OAS schema to process
     */
    protected void generateContainerSchemas(OpenAPI openAPI, Set<Schema> visitedSchemas, Schema schema) {
        if (visitedSchemas.contains(schema)) {
            return;
        }
        visitedSchemas.add(schema);

        if (schema != null) {
            //Dereference schema
            schema = ModelUtils.getReferencedSchema(openAPI, schema);
            Boolean isContainer = Boolean.FALSE;

            if (ModelUtils.isObjectSchema(schema)) {
                //Recursively process all schemas of object properties
                Map<String, Schema> properties = schema.getProperties();
                if (properties != null) {
                    for (String propertyName : properties.keySet()) {
                        generateContainerSchemas(openAPI, visitedSchemas, properties.get(propertyName));
                    }
                }
            } else if (ModelUtils.isArraySchema(schema)) {
                //Recursively process schema of array items
                generateContainerSchemas(openAPI, visitedSchemas, ModelUtils.getSchemaItems(schema));
                isContainer = Boolean.TRUE;
            } else if (ModelUtils.isMapSchema(schema)) {
                //Recursively process schema of map items
                Object itemSchema = schema.getAdditionalProperties();
                if (itemSchema instanceof Schema) {
                    generateContainerSchemas(openAPI, visitedSchemas, (Schema) itemSchema);
                }
                isContainer = Boolean.TRUE;
            }

            if (isContainer) {
                //Generate special component schema for container
                String containerSchemaName = generateUniqueSchemaName(openAPI, "Collection");
                Schema containerSchema = new ObjectSchema();
                containerSchema.addProperty("inner", schema);
                addInternalExtensionToSchema(containerSchema, VEN_FROM_CONTAINER, Boolean.TRUE);
                openAPI.getComponents().addSchemas(containerSchemaName, containerSchema);
                String containerDataType = getTypeDeclaration(toModelName(containerSchemaName));
                addInternalExtensionToSchema(schema, VEN_CONTAINER_DATA_TYPE, containerDataType);
            }
        }
    }
}
