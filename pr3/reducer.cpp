#include <iostream>
#include <map>
#include <string>
#include <sstream>

using namespace std;

typedef map< string, string > HashTable_t;
typedef HashTable_t::iterator HashTableIter_t;

int main() {
	HashTable_t hashtable;
	string word;

	do {
		cin >> word;
		if(cin) {
			stringstream ss;
			ss << hashtable[word] << cin;
			hashtable[word] = ss.str(); 
		}
	} while(cin);

	for(HashTableIter_t iter = hashtable.begin(); iter != hashtable.end(); ++iter) {
	cout << iter->first << "\t" << iter->second << endl;
	}
}
