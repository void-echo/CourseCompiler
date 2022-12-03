int main() { int x[3]={7,9,11}; x[3] = 13; return x[3];}

// how to gcc an assembly file gcc -S -masm=intel -o file.s file.