#include <iostream>
#include <thread>
#include <mutex>

using namespace std;

class singleton{
private:
    static singleton *instance;
    static mutex mtx;

    singleton(){
        cout << "constructor called" << endl;
    }

    // delete copy constructor
    singleton(const singleton& s) = delete;

    // delete assignment operator
    singleton& operator=(const singleton&) = delete;

public:
    static singleton *getInstance(){
        if(instance == nullptr){
            lock_guard<mutex> lock(mtx);
            if (instance == nullptr){
                instance = new singleton();
            }
        }
        return instance;
    }    
};

singleton *singleton::instance = nullptr;

int main(){
    singleton *obj1 = singleton::getInstance();
    singleton *obj2 = singleton::getInstance();

    if (obj1 == obj2){
        cout << "same object" << endl;
    }
    else
        cout << "different object created" << endl;

    return 0;
}