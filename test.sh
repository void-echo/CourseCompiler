#!/bin/bash
# Put it in the dir: target\classes
assert() {
    expected="$1"
    input="$2"

    echo "$input" | java com.example.coursecompiler.Main - >tmp.s || exit
    gcc -o tmp tmp.s
    ./tmp
    actual="$?"

    if [ "$actual" = "$expected" ]; then
        echo "$input => $actual"
    else
        echo "$input => $expected expected, but got $actual"
        exit 1
    fi
}

assert 4 'int main() {int a; a =5; int b; b =9-2-3;}'
assert 6 'int main() {int a; a =5; int b ; b= a +1;}'
assert 4 'int main() {int _a; _a=5; int b; b = _a - 1;}'
assert 8 'int main() {return 7+1;}'
assert 13 'int main() {return 7+2*3;}'
assert 6 'int main() {int a,b,c,d; a=3; return a+3; 9;}'
assert 9 'int main() {7; 8; 9;}'
assert 9 'int main() {int _a; _a =5; int b; b =9;}'
assert 5 'int main() {int a; a=2; {int b; b=a+3;return b;} }'

assert 33 'int main() {return foo(11,22); } int foo(int a, int b){ return a+b; }'
assert 21 'int foo(int a, int b, int c, int d, int e, int f){ return a+b+c+d+e+f;} int main() { return foo(1,2,3,4,5,6);} '

assert 3 'int main() {if(1) then return 3; else return 5;}'
assert 5 'int main() {if(0) then return 3; else return 5;}'
assert 5 'int main() {if(1>2) then return 3; else return 5;}'
assert 3 'int main() {if(1<2) then ; return 3;}'
assert 3 'int main() {if(1>2) then return 5; else ; return 3;}'
assert 7 'int main() { int x[1]={7}; return x[1];}'
assert 9 'int main() { int x[2]={7,9}; return x[2]; }'
assert 9 'int main() { int x[3]={7,9,11}; int temp; temp = x[2]; return temp;}'
assert 13 'int main() { int x[3]={7,9,11}; x[2] = 13; return x[2];}'
assert 4 'int main(){ int array[4] = {2,4,3,1}; int len; len = 4; int i; int min; min = 2; i=2; return array[min];}'

assert 11 'int main(){ bool b; b = 1; while (b) {return 11;} }'
echo OK
