package com.example.data.provider

import com.example.data.model.LanguageType
import com.example.data.model.Lesson
import com.example.data.model.QuizQuestion

object StaticDataProvider {

    val lessonsMap: Map<LanguageType, List<Lesson>> = mapOf(
        LanguageType.PYTHON to listOf(
            Lesson(
                id = 1,
                language = "Python",
                title = "Variables & Basic Types",
                subtitle = "Declaring dynamic data in Python",
                conceptExplain = "Python is a dynamically typed programming language. You don't need to specify variable types (like int or string) before using them. Simply write the variable name, assign a value to it using '=', and you are ready!\n\nStandard types include:\n• integers (e.g. 42)\n• floats (e.g. 3.14)\n• strings (enclosed in quotes)\n• booleans (True or False).",
                codeSnippet = "name = \"Codey\"\nage = 5\nheight = 1.62\nis_active = True\n\nprint(\"Name:\", name)\nprint(\"Age:\", age)\nprint(\"Height in meters:\", height)\nprint(\"Active Status:\", is_active)",
                expectedOutput = "Name: Codey\nAge: 5\nHeight in meters: 1.62\nActive Status: True"
            ),
            Lesson(
                id = 2,
                language = "Python",
                title = "Control Flow (If/Else)",
                subtitle = "Making decisions with programming logic",
                conceptExplain = "Logic gates let your program perform operations conditionally. Python uses the keywords 'if', 'elif' (else if), and 'else' for decision structures.\n\nUnlike languages like Java or JS, Python does NOT use curly braces {} to frame code blocks. Instead, it relies strictly on indentation (usually 4 spaces) and colons (:). Every indented line after a colon constitutes that block's scope.",
                codeSnippet = "score = 85\n\nif score >= 90:\n    print(\"Excellent! Grade: A\")\nelif score >= 80:\n    print(\"Great job! Grade: B\")\nelse:\n    print(\"Keep learning! Grade: C\")",
                expectedOutput = "Great job! Grade: B"
            ),
            Lesson(
                id = 3,
                language = "Python",
                title = "Loops (For & While)",
                subtitle = "Automating repetitive work through loops",
                conceptExplain = "Loops let you execute a body of code multiple times.\n\n• For loops: Perfect for iterating over ranges or sequences like lists. The 'range(x)' function yields a progression of integers starting from 0 up to (but not including) x.\n• While loops: Continue to repeat as long as a logic condition remains True.",
                codeSnippet = "# Using range to print counts\nprint(\"For loop output:\")\nfor i in range(3):\n    print(\"Current index:\", i)\n\n# Using a while loop\nprint(\"\\nWhile loop output:\")\ncount = 3\nwhile count > 0:\n    print(\"Countdown:\", count)\n    count = count - 1",
                expectedOutput = "For loop output:\nCurrent index: 0\nCurrent index: 1\nCurrent index: 2\n\nWhile loop output:\nCountdown: 3\nCountdown: 2\nCountdown: 1"
            ),
            Lesson(
                id = 4,
                language = "Python",
                title = "Functions",
                subtitle = "Creating reusable code modules",
                conceptExplain = "A function is a block of organized, reusable code that is used to perform a single, related action.\n\nIn Python:\n• Functions are defined using the 'def' keyword followed by the function name, parameters in parenthesis, and a colon.\n• A 'return' statement is used to pass a value back to the caller. If omitted, the function returns None.",
                codeSnippet = "def calculate_bonus(salary, rate):\n    return salary * rate\n\ndef greet(name):\n    print(\"Hello, \" + name + \"!\")\n\ngreet(\"Alice\")\nbonus = calculate_bonus(5000, 0.1)\nprint(\"Calculated Bonus:\", bonus)",
                expectedOutput = "Hello, Alice!\nCalculated Bonus: 500.0"
            )
        ),
        LanguageType.JAVA to listOf(
            Lesson(
                id = 1,
                language = "Java",
                title = "Anatomy of a Class",
                subtitle = "The structure of Object-Oriented Java programs",
                conceptExplain = "Java is a strongly typed, object-oriented language. Every single runnable line in Java MUST live inside a class.\n\nWhen a Java application launches, it searches for a entry method styled precisely as:\n'public static void main(String[] args)'\n\nTo write to standard console output, Java uses 'System.out.println(...)'. Every programmatic statement must terminate with a semicolon (;).",
                codeSnippet = "public class HelloCode {\n    public static void main(String[] args) {\n        System.out.println(\"Hello from Java Class!\");\n        System.out.println(\"Java is extremely robust.\");\n    }\n}",
                expectedOutput = "Hello from Java Class!\nJava is extremely robust."
            ),
            Lesson(
                id = 2,
                language = "Java",
                title = "Variables & Strict Typing",
                subtitle = "Declaring explicit registers in memory",
                conceptExplain = "Java enforces static typing. This means you must declare the variable type explicitly before assignment, and you cannot store other types in it later.\n\nJava core types include:\n• int (for integers like 5)\n• double (for decimals like 3.14)\n• String (wrapped in double quotes)\n• boolean (lowercase 'true' or 'false').",
                codeSnippet = "String student = \"Tharan\";\nint points = 95;\ndouble average = 92.5;\nboolean isCertified = true;\n\nSystem.out.println(\"Student: \" + student);\nSystem.out.println(\"Points: \" + points);\nSystem.out.println(\"Average: \" + average);\nSystem.out.println(\"Certified? \" + isCertified);",
                expectedOutput = "Student: Tharan\nPoints: 95\nAverage: 92.5\nCertified? true"
            ),
            Lesson(
                id = 3,
                language = "Java",
                title = "Java Control Structures",
                subtitle = "Executing conditional brackets & standard for loops",
                conceptExplain = "Java uses standard C-style loops and structures.\n\nCurly braces '{}' frame programmatic scopes. Brackets '( )' enclose conditional logical arguments. Loops can repeat blocks, utilizing logical increments like 'i++' (i = i + 1).",
                codeSnippet = "int status = 200;\n\nif (status == 200) {\n    System.out.println(\"Success! Processing data...\");\n} else {\n    System.out.println(\"Error connecting to server.\");\n}\n\nSystem.out.println(\"Running loop:\");\nfor (int i = 0; i < 3; i++) {\n    System.out.println(\"Iteration: \" + i);\n}",
                expectedOutput = "Success! Processing data...\nRunning loop:\nIteration: 0\nIteration: 1\nIteration: 2"
            ),
            Lesson(
                id = 4,
                language = "Java",
                title = "Static & Class Methods",
                subtitle = "Structuring operational functions with parameters",
                conceptExplain = "In Java, methods represent functions within a class block. If a method does not depend on a particular object instance state, it is marked 'static'.\n\nYou must explicitly declare the return type of the method (such as 'int', 'String') or write 'void' if it returns nothing.",
                codeSnippet = "public class LogicClass {\n    public static int multiply(int a, int b) {\n        return a * b;\n    }\n\n    public static void greetUser(String user) {\n        System.out.println(\"Warm welcome, \" + user);\n    }\n\n    public static void main(String[] args) {\n        greetUser(\"Java Developer\");\n        int result = multiply(6, 7);\n        System.out.println(\"Multiplying result: \" + result);\n    }\n}",
                expectedOutput = "Warm welcome, Java Developer\nMultiplying result: 42"
            )
        ),
        LanguageType.JAVASCRIPT to listOf(
            Lesson(
                id = 1,
                language = "JavaScript",
                title = "Modern Variables (let & const)",
                subtitle = "Dynamically scripting values on the web",
                conceptExplain = "JavaScript is a lightweight, dynamically typed prototype programming language. Today, we declare variables mainly using:\n• 'const' for static references that shouldn't be reassigned.\n• 'let' for variable assignments that might change.\n\nNever use the legacy 'var' keyword. Outputting logs uses 'console.log()'. Statements can end with semicolons, but ES6+ makes them optional in most scenarios.",
                codeSnippet = "const trainerName = \"Codey\";\nlet score = 100;\nscore = score + 50;\n\nconsole.log(\"Trainer:\", trainerName);\nconsole.log(\"Terminal score value:\", score);",
                expectedOutput = "Trainer: Codey\nTerminal score value: 150"
            ),
            Lesson(
                id = 2,
                language = "JavaScript",
                title = "Arrow Functions (=>)",
                subtitle = "Modern, functional shorthand notation in JS",
                conceptExplain = "ES6 introduced arrow functions, a concise syntax to write functions. Arrow functions syntax replaces 'function' with '=>' notation.\n\nParameters go on the left of '=>' and the code block or returned value goes on the right. If the body is single-line, the 'return' keyword and curly braces can be excluded entirely!",
                codeSnippet = "const formatName = (username) => \"@\" + username.toLowerCase();\n\nconst addNums = (a, b) => {\n    const sum = a + b;\n    return sum;\n};\n\nconsole.log(\"Username token:\", formatName(\"Naveen\"));\nconsole.log(\"Interactive Add result:\", addNums(8, 12));",
                expectedOutput = "Username token: @naveen\nInteractive Add result: 20"
            ),
            Lesson(
                id = 3,
                language = "JavaScript",
                title = "Visualizing Async & Promises",
                subtitle = "How JavaScript handles deferred non-blocking operations",
                conceptExplain = "JavaScript is single-threaded but handles asynchronous commands easily using Promises or the 'async/await' keyword wrappers.\n\nA Promise represents the future result of an asynchronous operation (like fetching web API details). It starts in a 'pending' state and eventually becomes 'fulfilled' (resolved) or 'rejected' (failed).",
                codeSnippet = "const mockNetworkCall = () => {\n    return Promise.resolve(\"Database user: Tharan loaded!\");\n};\n\nconsole.log(\"Requesting database user...\");\nmockNetworkCall().then(message => {\n    console.log(\"Result:\", message);\n});",
                expectedOutput = "Requesting database user...\nResult: Database user: Tharan loaded!"
            ),
            Lesson(
                id = 4,
                language = "JavaScript",
                title = "Array Map, Filter & Arrow Lambdas",
                subtitle = "Elegant array processing methodologies",
                conceptExplain = "Writing loops to process arrays is tedious. JavaScript lists feature functional handlers like:\n• 'map' to transform each element and return a new array.\n• 'filter' to return only the list items matching a logic statement.",
                codeSnippet = "const ages = [12, 18, 24, 30];\n\nconst doubled = ages.map(num => num * 2);\nconst adults = ages.filter(num => num >= 18);\n\nconsole.log(\"Doubled:\", doubled);\nconsole.log(\"Adults only:\", adults);",
                expectedOutput = "Doubled: [24, 36, 48, 60]\nAdults only: [18, 24, 30]"
            )
        ),
        LanguageType.HTML to listOf(
            Lesson(
                id = 1,
                language = "HTML",
                title = "Structure & Elements",
                subtitle = "The layout markup foundation of the world-wide web",
                conceptExplain = "HTML stands for HyperText Markup Language, forming the scaffolding of websites. Elements are defined using tags in angled brackets like <tagName>.\n\nMost tags require an opening tag and a closing tag with a slash prefix (e.g., <h1>Title</h1>). Nested elements represent hierarchy. Standard core layout elements include header, main body, and lists.",
                codeSnippet = "<h2>Web Dev Portfolio</h2>\n<p>Hi, I am Naveen! This is my first web page.</p>\n<a>Check my workspace!</a>",
                expectedOutput = "<!-- HTML RENDERS IN THE PLAYGROUND PREVIEW WINDOW -->"
            ),
            Lesson(
                id = 2,
                language = "HTML",
                title = "Images, CSS Styles & Attributes",
                subtitle = "Styling elements and giving properties",
                conceptExplain = "Tags can have attributes (properties) written inside the opening tag. A common property is 'style', allowing inline CSS properties like 'color', 'background-color', or 'font-family'.\n\nExample attributes:\n• 'href' on anchor tags <a>\n• 'src' on image tags <img>\nValues are enclosed in double quotes.",
                codeSnippet = "<div style=\"background-color: #3776AB; padding: 12px; border-radius: 8px; color: white;\">\n  <h3 style=\"margin: 0;\">Alert Banner</h3>\n  <p style=\"font-size: 14px; margin-top: 4px;\">Python module started successfully!</p>\n</div>",
                expectedOutput = "<!-- HTML RENDERS IN THE PLAYGROUND PREVIEW WINDOW -->"
            ),
            Lesson(
                id = 3,
                language = "HTML",
                title = "Forms & User Input",
                subtitle = "Building interface capture states",
                conceptExplain = "Forms are essential to get user text inputs and selections. We use the `<form>` element containing specific input fields:\n• `<input>` representing simple dynamic fields.\n• `<label>` formatting standard accessibility captions.\n• `<button>` for clicking submit triggers.",
                codeSnippet = "<div style=\"padding: 16px; border: 1px solid #ddd; border-radius: 12px;\">\n  <h4 style=\"margin: 0 0 8px 0; color: #333;\">Login Panel</h4>\n  <label style=\"display: block; font-size: 12px; font-weight: bold;\">Email Address</label>\n  <input type=\"text\" placeholder=\"user@example.com\" style=\"display: block; width: 100%; padding: 6px; margin: 6px 0; border: 1px solid #ccc;\" />\n  <button style=\"background-color: #007396; color: white; padding: 8px 16px; border: none; border-radius: 4px;\">Submit</button>\n</div>",
                expectedOutput = "<!-- HTML RENDERS IN THE PLAYGROUND PREVIEW WINDOW -->"
            ),
            Lesson(
                id = 4,
                language = "HTML",
                title = "Semantic Page Architecture",
                subtitle = "Creating layouts that are structured and searchable",
                conceptExplain = "Semantic elements describe their meaning to both the browser and the developer. They ensure search engines and screen readers can parse page indexes naturally.\n\nElements include: <article>, <aside>, <details>, <figcaption>, <figure>, <footer>, <header>, <main>, <mark>, <nav>, <section>, and <summary>.\nThey render like standard blocks, but represent structural units.",
                codeSnippet = "<section style=\"background-color: #f9f9f9; padding: 15px; border-radius: 6px;\">\n  <header><b style=\"color: #E34F26;\">HTML Concept 4</b></header>\n  <hr style=\"margin: 8px 0; border-top: 1px solid #aaa;\" />\n  <article>\n    Semantic HTML tags increase web indexing. It improves accessibility structures for screen users.\n  </article>\n</section>",
                expectedOutput = "<!-- HTML RENDERS IN THE PLAYGROUND PREVIEW WINDOW -->"
            )
        )
    )

    val quizzesMap: Map<LanguageType, List<QuizQuestion>> = mapOf(
        LanguageType.PYTHON to listOf(
            QuizQuestion(
                id = 1,
                language = "Python",
                question = "How do you declare variable scopes or blocks in Python?",
                options = listOf(
                    "By enclosing statements in curly brackets '{ }'",
                    "Using 'begin' and 'end' keywords",
                    "Through strict code indentation (spaces/tabs)",
                    "By ending each line with a colon ';'"
                ),
                correctAnswerIndex = 2,
                explanation = "Python does not use brackets for block definition. Code hierarchies rely completely on regular spacing indentation after colons (:)."
            ),
            QuizQuestion(
                id = 2,
                language = "Python",
                question = "Which keyword is used to define functions in Python?",
                options = listOf(
                    "function",
                    "def",
                    "func",
                    "define"
                ),
                correctAnswerIndex = 1,
                explanation = "In Python, functions are introduced with the 'def' keyword."
            ),
            QuizQuestion(
                id = 3,
                language = "Python",
                question = "What does the expression 'range(5)' represent in Python loop constructs?",
                options = listOf(
                    "A sequence of numbers from 0 to 5 inclusive",
                    "A sequence of numbers from 1 to 5",
                    "A sequence of numbers from 0 to 4",
                    "An array with five empty spots"
                ),
                correctAnswerIndex = 2,
                explanation = "'range(5)' produces numbers starting at 0 up to (but not including) 5: [0, 1, 2, 3, 4]."
            )
        ),
        LanguageType.JAVA to listOf(
            QuizQuestion(
                id = 1,
                language = "Java",
                question = "What is the mandatory method signature required to run a Java application?",
                options = listOf(
                    "public void main(String args)",
                    "public static void main(String[] args)",
                    "static void main(string argv)",
                    "public static int main(String[] args)"
                ),
                correctAnswerIndex = 1,
                explanation = "An executable entry point in Java must be exactly 'public static void main(String[] args)'."
            ),
            QuizQuestion(
                id = 2,
                language = "Java",
                question = "Which statement is true regarding variable declarations in Java?",
                options = listOf(
                    "Java variables are dynamically typed like python",
                    "You cannot reassign double variables",
                    "Variable types must be declared explicitly (strictly typed)",
                    "Variables can hold any type of data once set"
                ),
                correctAnswerIndex = 2,
                explanation = "Java is statically and strictly typed. You must declare the exact type of a variable before assigning its value."
            ),
            QuizQuestion(
                id = 3,
                language = "Java",
                question = "What does 'void' mean in a Java method signature?",
                options = listOf(
                    "The method returns an empty object state",
                    "The method does not return any value",
                    "The method can return any data format",
                    "The method is skipped during execution"
                ),
                correctAnswerIndex = 1,
                explanation = "A method marked with a 'void' return type perform actions but does not return any data to its caller."
            )
        ),
        LanguageType.JAVASCRIPT to listOf(
            QuizQuestion(
                id = 1,
                language = "JavaScript",
                question = "Which keywords are preferred when declaring modern variables in ES6+?",
                options = listOf(
                    "var and let",
                    "const and dynamic",
                    "let and const",
                    "var and const"
                ),
                correctAnswerIndex = 2,
                explanation = "Modern JavaScript uses 'let' for reassignable configurations and 'const' for static descriptors. 'var' is legacy and avoided."
            ),
            QuizQuestion(
                id = 2,
                language = "JavaScript",
                question = "Which of the following is a key character of Arrow functions?",
                options = listOf(
                    "They cannot return values",
                    "They feature a concise '=>' syntactic notation",
                    "They require brackets in every declaration",
                    "They are not supported in web browsers"
                ),
                correctAnswerIndex = 1,
                explanation = "Arrow functions are a modern shorthand syntax in ES6 utilizing '=>' parameters structures."
            ),
            QuizQuestion(
                id = 3,
                language = "JavaScript",
                question = "What is a Promise in JavaScript asynchronous models?",
                options = listOf(
                    "An internal lock that freezes processing thread",
                    "A placeholder object that represents future result coordinates",
                    "An alert popup displayed on window actions",
                    "A function that returns integers automatically"
                ),
                correctAnswerIndex = 1,
                explanation = "A Promise is a placeholder representing the future completion (resolution) or failure of an asynchronous operations stack."
            )
        ),
        LanguageType.HTML to listOf(
            QuizQuestion(
                id = 1,
                language = "HTML",
                question = "What does HTML stand for?",
                options = listOf(
                    "Hyper Transfer Markup Language",
                    "HyperText Markdown Language",
                    "HyperText Markup Language",
                    "High Tech Markup Links"
                ),
                correctAnswerIndex = 2,
                explanation = "HTML stands for 'HyperText Markup Language' and is used to structure content on web pages."
            ),
            QuizQuestion(
                id = 2,
                language = "HTML",
                question = "How do you define an attribute (or property) in an HTML tag?",
                options = listOf(
                    "By writing style details inside curly brackets after the tag",
                    "Written inside the opening tag as a name/value equation",
                    "Defined at the footer block of elements",
                    "By separating items with hashtags '#'"
                ),
                correctAnswerIndex = 1,
                explanation = "Attributes are specified in the opening tag to provide administrative properties (e.g. style=\"color: red;\")."
            ),
            QuizQuestion(
                id = 3,
                language = "HTML",
                question = "What is the core benefit of selecting 'semantic' tags like <section> or <article> over normal <div> tags?",
                options = listOf(
                    "Semantic elements are faster to load syntactically",
                    "They automatically inject styles into the browser",
                    "They provide clean layout structure and boost accessibility & SEO",
                    "Semantic layout limits script usage pages"
                ),
                correctAnswerIndex = 2,
                explanation = "Semantic HTML outlines the page's exact structure, helping search engines (SEO) and readers (Accessibility) index context properly."
            )
        )
    )
}
