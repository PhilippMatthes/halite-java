import os
import sys
import threading

import hlt_client.hlt_client.compare_bots as compare

path_to_binary = "/Users/philippmatthes/desktop/halite-java/halite"
path_to_bot = "/Users/philippmatthes/desktop/halite-java/MyBot"
run_bot_command = "java " + path_to_bot
map_height = 160
map_width = 240
tries = 10

number_of_threads = 8




def task():
    print(compare.play_games(path_to_binary, map_height, map_width, [run_bot_command, run_bot_command], tries))


if __name__ == "__main__":

    os.system("javac " + path_to_bot + ".java")

    threads = []

    for thread_number in range(number_of_threads):
        threads.append(threading.Thread(target=task))

    for thread in threads:
        thread.start()

    for thread in threads:
        thread.join()

    os.system("find . -name \*.hlt -delete; find . -name \*.log -delete; find . -name \*.class -delete")
