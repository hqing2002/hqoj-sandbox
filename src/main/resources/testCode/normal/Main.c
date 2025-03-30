#include <iostream>
#include <cstdlib>

int main(int argc, char **argv) {
  int sum = 0;
  for (int i = 1; i < argc; ++i) {
  int num = atoi(argv[i]);
    sum += num;
  }
  std::cout << "Sum: " << sum << std::endl;
  return 0;
}
