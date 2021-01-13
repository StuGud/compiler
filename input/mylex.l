digit       [0-9]
letter      [A-Za-z]
id          {letter}({letter}|{digit})*
number      {digit}+(\\.{digit}+)?(E[+-]?{digit}+)?

%{
#include <string>
#include <map>
#include <iostream>
int line = 0;
int numCount = 0;
%}


%%

int         { return "int"; }
float       { return "float"; }
double      { return "double"; }
void        { return "void"; }
if          { return "if"; }
else        { return "else"; }
while       { return "while"; }
return      { return "return"; }
static      { return "static"; }
{id}        { return "id"; }
{number}    { ++numCount; return "num"; }
"+"         { return "+"; }
"-"         { return "-"; }
"*"         { return "*"; }
"/"         { return "/"; }
"%"         { return "%"; }
"+="        { return "+="; }
"-="        { return "-="; }
"*="        { return "*="; }
"/="        { return "/="; }
"%="        { return "%="; }
"&&"        { return "&&"; }
"||"        { return "||"; }
"!"         { return "!"; }
"<="        { return "<="; }
">="        { return ">="; }
"="         { return "="; }
"!="        { return "!="; }
"=="        { return "=="; }
"<"         { return "<"; }
">"         { return ">"; }
"("         { return "("; }
")"         { return ")"; }
"{"         { return "{"; }
"}"         { return "}"; }
"["         { return "["; }
"]"         { return "]"; }
";"         { ++line;return ";"; }
","         { return ","; }

%%

int yywrap(void)
{
	return 1;
}
void comment(void)
{
	char c, prev = 0;
  
	while ((c = input()) != 0)      /* (EOF maps to 0) */
	{
		if (c == '/' && prev == '*')
			return;
		prev = c;
	}
	error("unterminated comment");
}


int column = 0;

void count(void)
{
	int i;

	for (i = 0; yytext[i] != '\0'; i++)
		if (yytext[i] == '\n')
			column = 0;
		else if (yytext[i] == '\t')
			column += 8 - (column % 8);
		else
			column++;

	ECHO;
}


int check_type(void)
{
/*
* pseudo code --- this is what it should check
*
*	if (yytext == type_name)
*		return TYPE_NAME;
*
*	return IDENTIFIER;
*/

/*
*	it actually will only return IDENTIFIER
*/

	return IDENTIFIER;
}
void calLine() {
	std::cout << "number of semicolon : " << line << std::endl;
}

void getNumCount() {
    std::cout << "count of num : " << numCount << std::endl;
}

void main() {
	calLine();
	getNumCount();
}