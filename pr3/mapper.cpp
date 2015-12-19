#include <iostream>
#include <string>
#include <stdlib.h>

using namespace std;

int main() {
	string word;
	int offset = 0;
	string filename = getenv("map_input_file");

	do {
		cin >> word;
		if(cin) {
			//Print the location and update the offset
			cout << word << "\t" << filename << ":" << offset << endl;
			offset += word.length() + 1;
		}
	} while (cin);
}
