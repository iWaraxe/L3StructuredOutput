#!/bin/bash

# Script to update Spring AI version to 1.0.0 in all branches

BRANCHES=(
    "02-prompt-templates"
    "03-structured-output-fundamentals"
    "04-structured-output-converters"
    "05-bean-output-converter"
    "06-other-output-converters"
    "07-chat-client-and-model"
    "08-model-specific-json-modes"
)

echo "Starting Spring AI 1.0.0 update for all branches..."

for branch in "${BRANCHES[@]}"; do
    echo ""
    echo "========================================="
    echo "Processing branch: $branch"
    echo "========================================="
    
    # Checkout the branch
    git checkout "$branch"
    
    # Update Spring AI version in pom.xml
    sed -i '' 's/<spring-ai.version>1.0.0-M7<\/spring-ai.version>/<spring-ai.version>1.0.0<\/spring-ai.version>/' pom.xml
    
    # Try to compile
    echo "Compiling $branch..."
    if ./mvnw clean compile -q; then
        echo "✅ Compilation successful for $branch"
        
        # Commit changes
        git add pom.xml
        git commit -m "Update Spring AI to version 1.0.0"
        echo "✅ Changes committed for $branch"
    else
        echo "❌ Compilation failed for $branch - manual intervention needed"
        # Save the error output
        ./mvnw clean compile > "compile-error-$branch.log" 2>&1
        echo "Error log saved to compile-error-$branch.log"
    fi
done

echo ""
echo "========================================="
echo "Update process completed!"
echo "========================================="

# Return to the original branch
git checkout 09-refactoring-into-sections