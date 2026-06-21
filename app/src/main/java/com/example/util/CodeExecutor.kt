package com.example.util

import com.example.data.model.LanguageType
import java.util.Locale

object CodeExecutor {

    data class ExecutionResult(
        val success: Boolean,
        val output: String,
        val errors: String = ""
    )

    fun execute(code: String, language: LanguageType): ExecutionResult {
        if (code.trim().isEmpty()) {
            return ExecutionResult(true, "No code to run. Write something in the console!")
        }

        return when (language) {
            LanguageType.PYTHON -> executePython(code)
            LanguageType.JAVA -> executeJava(code)
            LanguageType.JAVASCRIPT -> executeJavaScript(code)
            LanguageType.HTML -> executeHtml(code)
        }
    }

    private fun executePython(code: String): ExecutionResult {
        val lines = code.split("\n")
        val outputLines = mutableListOf<String>()
        val variables = mutableMapOf<String, Any>()

        try {
            var i = 0
            while (i < lines.size) {
                val line = lines[i].trim()
                if (line.isEmpty() || line.startsWith("#")) {
                    i++
                    continue
                }

                // Handle single variables: x = 10 or name = "Codey" or count = count - 1
                if (line.contains("=") && !line.startsWith("if") && !line.startsWith("print")) {
                    val parts = line.split("=", limit = 2)
                    if (parts.size == 2) {
                        val varName = parts[0].trim()
                        val varValueStr = parts[1].trim()

                        val resolvedValue = evaluateValue(varValueStr, variables)
                        variables[varName] = resolvedValue
                    }
                }

                // Handle basic if score >= 80 logic:
                else if (line.startsWith("if ")) {
                    val cond = line.substring(3, line.length - 1).trim() // strip ':'
                    val conditionMet = evaluateCondition(cond, variables)
                    
                    // Skip or run matching block
                    var blockFinished = false
                    val runBlock = conditionMet
                    
                    i++ // move to block lines
                    while (i < lines.size && (lines[i].startsWith(" ") || lines[i].startsWith("\t") || lines[i].trim().isEmpty())) {
                        val blockLine = lines[i].trim()
                        if (runBlock && blockLine.isNotEmpty() && blockLine.startsWith("print")) {
                            executePythonPrint(blockLine, variables, outputLines)
                        }
                        i++
                    }
                    blockFinished = true
                    
                    // Handle elif/else blocks
                    while (i < lines.size && (lines[i].trim().startsWith("elif") || lines[i].trim().startsWith("else"))) {
                        val nextLine = lines[i].trim()
                        val isElif = nextLine.startsWith("elif")
                        val runThisBlock = if (runBlock) {
                            false // already executed if block
                        } else if (isElif) {
                            val elifCond = nextLine.substring(4, nextLine.length - 1).trim()
                            evaluateCondition(elifCond, variables)
                        } else {
                            true // else block
                        }
                        
                        i++
                        while (i < lines.size && (lines[i].startsWith(" ") || lines[i].startsWith("\t") || lines[i].trim().isEmpty())) {
                            val blockLine = lines[i].trim()
                            if (runThisBlock && blockLine.isNotEmpty() && blockLine.startsWith("print")) {
                                executePythonPrint(blockLine, variables, outputLines)
                            }
                            i++
                        }
                    }
                    continue // we already incremented i inside the loops
                }

                // Handle simple loops: for i in range(3):
                else if (line.startsWith("for ") && line.contains("in range(")) {
                    val forParts = line.substring(4).split(" in range(")
                    if (forParts.size == 2) {
                        val iterVar = forParts[0].trim()
                        val rangeMaxStr = forParts[1].replace("):", "").trim()
                        val rangeMax = rangeMaxStr.toIntOrNull() ?: 0
                        
                        // Capture block
                        val loopBlock = mutableListOf<String>()
                        i++
                        while (i < lines.size && (lines[i].startsWith(" ") || lines[i].startsWith("\t") || lines[i].trim().isEmpty())) {
                            if (lines[i].trim().isNotEmpty()) {
                                loopBlock.add(lines[i].trim())
                            }
                            i++
                        }
                        
                        // Execute loop
                        for (idx in 0 until rangeMax) {
                            variables[iterVar] = idx
                            for (bLine in loopBlock) {
                                if (bLine.startsWith("print")) {
                                    executePythonPrint(bLine, variables, outputLines)
                                } else if (bLine.contains("=")) {
                                    val parts = bLine.split("=", limit = 2)
                                    if (parts.size == 2) {
                                        variables[parts[0].trim()] = evaluateValue(parts[1].trim(), variables)
                                    }
                                }
                            }
                        }
                        continue // bypass standard increment
                    }
                }

                // Handle standard prints: print(...)
                else if (line.startsWith("print(")) {
                    executePythonPrint(line, variables, outputLines)
                }

                i++
            }

            val finalOutput = if (outputLines.isEmpty()) {
                "Code executed successfully with no print outputs."
            } else {
                outputLines.joinToString("\n")
            }

            return ExecutionResult(true, finalOutput)

        } catch (e: Exception) {
            return ExecutionResult(false, "", "Python Runtime Error: ${e.message}")
        }
    }

    private fun executePythonPrint(line: String, variables: Map<String, Any>, outputLines: MutableList<String>) {
        val contentsStr = line.substring(6, line.length - 1).trim()
        val tokens = splitPrintArguments(contentsStr)
        val resolvedTokens = tokens.map { token ->
            val cleanToken = token.trim()
            if (cleanToken.startsWith("\"") && cleanToken.endsWith("\"") || cleanToken.startsWith("'") && cleanToken.endsWith("'")) {
                cleanToken.substring(1, cleanToken.length - 1)
            } else {
                variables[cleanToken]?.toString() ?: cleanToken
            }
        }
        outputLines.add(resolvedTokens.joinToString(" "))
    }

    private fun executeJava(code: String): ExecutionResult {
        val lines = code.split("\n")
        val outputLines = mutableListOf<String>()
        val variables = mutableMapOf<String, Any>()

        try {
            var i = 0
            while (i < lines.size) {
                var line = lines[i].trim()
                if (line.isEmpty() || line.startsWith("//") || line.startsWith("public class") || line.startsWith("}") || line.contains("public static void main")) {
                    i++
                    continue
                }

                // clean trailing semicolon if exists
                if (line.endsWith(";")) {
                    line = line.substring(0, line.length - 1).trim()
                }

                // Variable types: String trainer = "Codey" or int x = 3
                val typeAndAssign = line.split(" ", limit = 2)
                if (typeAndAssign.size == 2 && isJavaType(typeAndAssign[0])) {
                    val activeAssignment = typeAndAssign[1].trim()
                    val parts = activeAssignment.split("=", limit = 2)
                    if (parts.size == 2) {
                        val varName = parts[0].trim()
                        val varValueStr = parts[1].trim()
                        variables[varName] = evaluateValue(varValueStr, variables)
                    }
                }
                // Plain reassignments: x = x + 1
                else if (line.contains("=") && !line.startsWith("if") && !line.startsWith("System.out.println") && !line.startsWith("for")) {
                    val parts = line.split("=", limit = 2)
                    if (parts.size == 2) {
                        val varName = parts[0].trim()
                        val varValueStr = parts[1].trim()
                        variables[varName] = evaluateValue(varValueStr, variables)
                    }
                }
                // Handle Prints: System.out.println(...)
                else if (line.startsWith("System.out.println(")) {
                    val printArg = line.substring(19, line.length - 1).trim()
                    val resolved = evaluateConcatenation(printArg, variables)
                    outputLines.add(resolved)
                }
                // Handle For Loops: for (int i = 0; i < 3; i++) {
                else if (line.startsWith("for") && line.contains("(") && line.contains(")")) {
                    val contentBetweenParentheses = line.substring(line.indexOf('(') + 1, line.lastIndexOf(')')).trim()
                    val sections = contentBetweenParentheses.split(";")
                    if (sections.size == 3) {
                        // init statement: int i = 0
                        val initSec = sections[0].trim()
                        val isInt = initSec.startsWith("int ")
                        val assignParts = if (isInt) initSec.substring(4).split("=") else initSec.split("=")
                        val iterVar = assignParts[0].trim()
                        val startVal = evaluateValue(assignParts[1].trim(), variables).toString().toIntOrNull() ?: 0
                        variables[iterVar] = startVal

                        // limit check: i < 3
                        val checkSec = sections[1].trim()
                        // increment: i++ or i = i + 1
                        val incrSec = sections[2].trim()

                        // capture simple loop block
                        val loopBlock = mutableListOf<String>()
                        i++ // advance from definitions
                        var openBraces = 1
                        while (i < lines.size && openBraces > 0) {
                            val listLine = lines[i].trim()
                            if (listLine.contains("{")) openBraces++
                            if (listLine.contains("}")) openBraces--
                            
                            val cleanedBlock = listLine.replace("{", "").replace("}", "").trim()
                            if (cleanedBlock.isNotEmpty()) {
                                loopBlock.add(cleanedBlock)
                            }
                            i++
                        }

                        // Loop mock execution
                        var loopCount = 0
                        while (true) {
                            val activeVal = variables[iterVar] as? Int ?: 0
                            val testCondition = checkSec.replace(iterVar, activeVal.toString())
                            if (!evaluateCondition(testCondition, variables) || loopCount > 200) {
                                break
                            }

                            // run loop body
                            for (bLine in loopBlock) {
                                var runLine = bLine
                                if (runLine.endsWith(";")) runLine = runLine.substring(0, runLine.length - 1).trim()
                                
                                if (runLine.startsWith("System.out.println(")) {
                                    val printArg = runLine.substring(19, runLine.length - 1).trim()
                                    val resolved = evaluateConcatenation(printArg, variables)
                                    outputLines.add(resolved)
                                } else if (runLine.contains("=")) {
                                    val parts = runLine.split("=", limit = 2)
                                    if (parts.size == 2) {
                                        variables[parts[0].trim()] = evaluateValue(parts[1].trim(), variables)
                                    }
                                }
                            }

                            // Do increment
                            val iterVal = variables[iterVar] as? Int ?: 0
                            if (incrSec.contains("++")) {
                                variables[iterVar] = iterVal + 1
                            } else if (incrSec.contains("=")) {
                                val eqParts = incrSec.split("=")
                                variables[iterVar] = evaluateValue(eqParts[1].trim(), variables).toString().toIntOrNull() ?: 0
                            }
                            loopCount++
                        }
                        continue
                    }
                }

                i++
            }

            val finalOutput = if (outputLines.isEmpty()) {
                "Java process finished with exit code 0."
            } else {
                outputLines.joinToString("\n")
            }

            return ExecutionResult(true, finalOutput)

        } catch (e: Exception) {
            return ExecutionResult(false, "", "Java Compiler Error: ${e.message}")
        }
    }

    private fun isJavaType(word: String): Boolean {
        return word == "int" || word == "double" || word == "float" || word == "String" || word == "boolean" || word == "char"
    }

    private fun executeJavaScript(code: String): ExecutionResult {
        val lines = code.split("\n")
        val outputLines = mutableListOf<String>()
        val variables = mutableMapOf<String, Any>()

        try {
            var i = 0
            while (i < lines.size) {
                var line = lines[i].trim()
                if (line.isEmpty() || line.startsWith("//") || line.startsWith("/*") || line.startsWith("*/") || line.startsWith("}")) {
                    i++
                    continue
                }

                if (line.endsWith(";")) {
                    line = line.substring(0, line.length - 1).trim()
                }

                // Variables: const user = "Alice" or let value = 15
                if (line.startsWith("const ") || line.startsWith("let ") || line.startsWith("var ")) {
                    val cleanAssign = line.substring(line.indexOf(' ') + 1).trim()
                    val parts = cleanAssign.split("=", limit = 2)
                    if (parts.size == 2) {
                        val varName = parts[0].trim()
                        val varValueStr = parts[1].trim()
                        variables[varName] = evaluateValue(varValueStr, variables)
                    }
                }
                // Standard reassignment
                else if (line.contains("=") && !line.startsWith("console.log")) {
                    val parts = line.split("=", limit = 2)
                    if (parts.size == 2) {
                        val varName = parts[0].trim()
                        val varValueStr = parts[1].trim()
                        variables[varName] = evaluateValue(varValueStr, variables)
                    }
                }
                // Console log: console.log(...)
                else if (line.startsWith("console.log(")) {
                    val printArg = line.substring(12, line.length - 1).trim()
                    val resolved = evaluateConcatenation(printArg, variables)
                    outputLines.add(resolved)
                }

                i++
            }

            val finalOutput = if (outputLines.isEmpty()) {
                "JavaScript engine complete."
            } else {
                outputLines.joinToString("\n")
            }

            return ExecutionResult(true, finalOutput)
        } catch (e: Exception) {
            return ExecutionResult(false, "", "JavaScript SyntaxError: ${e.message}")
        }
    }

    private fun executeHtml(code: String): ExecutionResult {
        // HTML is renderable visual asset rather than raw terminal output process.
        // We output instructions indicating render
        return ExecutionResult(
            success = true,
            output = "HTML Render Frame triggered successfully. Click 'Render Pane' tab below to view standard outputs live!"
        )
    }

    // Helper: evaluate expressions or types safely
    private fun evaluateValue(input: String, variables: Map<String, Any>): Any {
        val cleanInput = input.trim()

        // Handle string literals
        if (cleanInput.startsWith("\"") && cleanInput.endsWith("\"") || cleanInput.startsWith("'") && cleanInput.endsWith("'")) {
            return cleanInput.substring(1, cleanInput.length - 1)
        }

        // Handle boolean literals
        if (cleanInput.equals("true", ignoreCase = true) || cleanInput.equals("True", ignoreCase = true)) {
            return true
        }
        if (cleanInput.equals("false", ignoreCase = true) || cleanInput.equals("False", ignoreCase = true)) {
            return false
        }

        // Handle simple arithmetic formulas like "score + 50" or "count - 1" or "salary * rate"
        val arithmeticOps = listOf("+", "-", "*", "/")
        for (op in arithmeticOps) {
            if (cleanInput.contains(op)) {
                val parts = cleanInput.split(op, limit = 2)
                val leftVal = evaluateValue(parts[0].trim(), variables)
                val rightVal = evaluateValue(parts[1].trim(), variables)

                val leftNum = leftVal.toString().toDoubleOrNull()
                val rightNum = rightVal.toString().toDoubleOrNull()

                if (leftNum != null && rightNum != null) {
                    return when (op) {
                        "+" -> leftNum + rightNum
                        "-" -> leftNum - rightNum
                        "*" -> leftNum * rightNum
                        "/" -> if (rightNum != 0.0) leftNum / rightNum else 0.0
                        else -> 0.0
                    }
                } else if (op == "+") {
                    // String concatenation fallback
                    return leftVal.toString() + rightVal.toString()
                }
            }
        }

        // Resolve single variable or return literal number
        if (variables.containsKey(cleanInput)) {
            return variables[cleanInput]!!
        }

        return cleanInput.toDoubleOrNull() ?: cleanInput.toIntOrNull() ?: cleanInput
    }

    private fun evaluateCondition(condition: String, variables: Map<String, Any>): Boolean {
        val cleanCond = condition.trim()

        // Check operators
        val operators = listOf(">=", "<=", "==", "!=", ">", "<")
        for (op in operators) {
            if (cleanCond.contains(op)) {
                val parts = cleanCond.split(op, limit = 2)
                val left = evaluateValue(parts[0].trim(), variables).toString().toDoubleOrNull() ?: 0.0
                val right = evaluateValue(parts[1].trim(), variables).toString().toDoubleOrNull() ?: 0.0

                return when (op) {
                    ">=" -> left >= right
                    "<=" -> left <= right
                    "==" -> left == right
                    "!=" -> left != right
                    ">" -> left > right
                    "<" -> left < right
                    else -> false
                }
            }
        }
        return false
    }

    private fun evaluateConcatenation(argument: String, variables: Map<String, Any>): String {
        // Handle comma or '+' joins
        val tokens = splitPrintArguments(argument)
        val resolved = tokens.map {
            val cleanToken = it.trim()
            if (cleanToken.startsWith("\"") && cleanToken.endsWith("\"") || cleanToken.startsWith("'") && cleanToken.endsWith("'")) {
                cleanToken.substring(1, cleanToken.length - 1)
            } else {
                variables[cleanToken]?.toString() ?: cleanToken
            }
        }
        return resolved.joinToString("")
    }

    private fun splitPrintArguments(args: String): List<String> {
        val list = mutableListOf<String>()
        var currentToken = StringBuilder()
        var insideQuotes = false
        var quoteChar = ' '
        var i = 0

        while (i < args.size) {
            val char = args[i]
            if (char == '"' || char == '\'') {
                if (!insideQuotes) {
                    insideQuotes = true
                    quoteChar = char
                } else if (char == quoteChar) {
                    insideQuotes = false
                }
                currentToken.append(char)
            } else if ((char == ',' || char == '+') && !insideQuotes) {
                list.add(currentToken.toString().trim())
                currentToken = StringBuilder()
            } else {
                currentToken.append(char)
            }
            i++
        }
        if (currentToken.isNotEmpty()) {
            list.add(currentToken.toString().trim())
        }
        return list
    }
}

// Extension to find string size on API levels below 26 if string length is referenced
private val String.size: Int
    get() = length
