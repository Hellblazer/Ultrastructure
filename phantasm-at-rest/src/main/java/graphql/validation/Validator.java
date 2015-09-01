package graphql.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.chiralbehaviors.CoRE.phantasm.graphql.VariablesAreBoundRule;

import graphql.language.Document;
import graphql.schema.GraphQLSchema;
import graphql.validation.rules.ArgumentsOfCorrectType;
import graphql.validation.rules.FieldsOnCorrectType;
import graphql.validation.rules.FragmentsOnCompositeType;
import graphql.validation.rules.KnownArgumentNames;
import graphql.validation.rules.KnownFragmentNames;
import graphql.validation.rules.KnownTypeNames;
import graphql.validation.rules.NoFragmentCycles;
import graphql.validation.rules.NoUndefinedVariables;
import graphql.validation.rules.NoUnusedFragments;
import graphql.validation.rules.NoUnusedVariables;
import graphql.validation.rules.OverlappingFieldsCanBeMerged;
import graphql.validation.rules.PossibleFragmentSpreads;
import graphql.validation.rules.ProvidedNonNullArguments;
import graphql.validation.rules.ScalarLeafs;
import graphql.validation.rules.VariableDefaultValuesOfCorrectType;
import graphql.validation.rules.VariableTypesMatchRule;
import graphql.validation.rules.VariablesAreInputTypes;

public class Validator {

    public List<ValidationError> validateDocument(GraphQLSchema schema,
                                                  Document document,
                                                  Map<String, Object> arguments) {
        ValidationContext validationContext = new ValidationContext(schema,
                                                                    document);

        ValidationErrorCollector validationErrorCollector = new ValidationErrorCollector();
        List<AbstractRule> rules = createRules(validationContext,
                                               validationErrorCollector,
                                               arguments);
        LanguageTraversal languageTraversal = new LanguageTraversal();
        languageTraversal.traverse(document,
                                   new RulesVisitor(validationContext, rules));

        return validationErrorCollector.getErrors();
    }

    private List<AbstractRule> createRules(ValidationContext validationContext,
                                           ValidationErrorCollector validationErrorCollector,
                                           Map<String, Object> arguments) {
        List<AbstractRule> rules = new ArrayList<>();
        ArgumentsOfCorrectType argumentsOfCorrectType = new ArgumentsOfCorrectType(validationContext,
                                                                                   validationErrorCollector);
        rules.add(argumentsOfCorrectType);
        VariableDefaultValuesOfCorrectType variableDefaultValuesOfCorrectType = new VariableDefaultValuesOfCorrectType(validationContext,
                                                                                                                       validationErrorCollector);
        rules.add(variableDefaultValuesOfCorrectType);
        FieldsOnCorrectType fieldsOnCorrectType = new FieldsOnCorrectType(validationContext,
                                                                          validationErrorCollector);
        rules.add(fieldsOnCorrectType);
        FragmentsOnCompositeType fragmentsOnCompositeType = new FragmentsOnCompositeType(validationContext,
                                                                                         validationErrorCollector);
        rules.add(fragmentsOnCompositeType);
        KnownArgumentNames knownArgumentNames = new KnownArgumentNames(validationContext,
                                                                       validationErrorCollector);
        rules.add(knownArgumentNames);
        KnownFragmentNames knownFragmentNames = new KnownFragmentNames(validationContext,
                                                                       validationErrorCollector);
        rules.add(knownFragmentNames);
        KnownTypeNames knownTypeNames = new KnownTypeNames(validationContext,
                                                           validationErrorCollector);
        rules.add(knownTypeNames);
        NoFragmentCycles noFragmentCycles = new NoFragmentCycles(validationContext,
                                                                 validationErrorCollector);
        rules.add(noFragmentCycles);
        NoUndefinedVariables noUndefinedVariables = new NoUndefinedVariables(validationContext,
                                                                             validationErrorCollector);
        rules.add(noUndefinedVariables);
        NoUnusedFragments noUnusedFragments = new NoUnusedFragments(validationContext,
                                                                    validationErrorCollector);
        rules.add(noUnusedFragments);
        NoUnusedVariables noUnusedVariables = new NoUnusedVariables(validationContext,
                                                                    validationErrorCollector);
        rules.add(noUnusedVariables);
        OverlappingFieldsCanBeMerged overlappingFieldsCanBeMerged = new OverlappingFieldsCanBeMerged(validationContext,
                                                                                                     validationErrorCollector);
        rules.add(overlappingFieldsCanBeMerged);
        PossibleFragmentSpreads possibleFragmentSpreads = new PossibleFragmentSpreads(validationContext,
                                                                                      validationErrorCollector);
        rules.add(possibleFragmentSpreads);
        ProvidedNonNullArguments providedNonNullArguments = new ProvidedNonNullArguments(validationContext,
                                                                                         validationErrorCollector);
        rules.add(providedNonNullArguments);
        ScalarLeafs scalarLeafs = new ScalarLeafs(validationContext,
                                                  validationErrorCollector);
        rules.add(scalarLeafs);
        VariablesAreInputTypes variablesAreInputTypes = new VariablesAreInputTypes(validationContext,
                                                                                   validationErrorCollector);
        rules.add(variablesAreInputTypes);
        VariableTypesMatchRule variableTypesMatchRule = new VariableTypesMatchRule(validationContext,
                                                                                   validationErrorCollector);
        rules.add(variableTypesMatchRule);
        VariablesAreBoundRule variablesAreBoundRule = new VariablesAreBoundRule(validationContext,
                                                                                validationErrorCollector,
                                                                                arguments);
        rules.add(variablesAreBoundRule);
        return rules;
    }
}