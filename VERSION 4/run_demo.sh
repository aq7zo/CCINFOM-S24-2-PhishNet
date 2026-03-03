#!/bin/bash

echo "========================================"
echo "  PhishNet Argon2 Demo Runner"
echo "========================================"
echo ""

echo "Compiling project..."
mvn clean compile -q
if [ $? -ne 0 ]; then
    echo "ERROR: Compilation failed!"
    exit 1
fi

echo ""
echo "Running Argon2 Demo..."
echo ""
mvn exec:java -Dexec.mainClass="util.DemoArgon2" -q

echo ""
echo "========================================"
echo "  Demo Complete!"
echo "========================================"


