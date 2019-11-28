int add(int x, int y) {
	int z ;
	z = x+y;
	return z;
}

int sum(int n)
{
    int i=0;
    int sum=0;
    while(i<n)
    {
        sum = sum + i;
        ++i;
    }
    return sum;
}

void main () {
	int t = 33;
	_print(add(1,t));
	_print(sum(100));
}
