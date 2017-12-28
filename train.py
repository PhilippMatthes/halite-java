import os
import sys
import threading
import random
from itertools import chain
from shutil import copyfile

import hlt_client.hlt_client.compare_bots as compare

path_to_binary = "/Users/philippmatthes/desktop/halite-java/halite"
path_to_bot = "MyBot"
run_bot_command = "java " + path_to_bot
map_height = 160
map_width = 240
tries = 10

randomization = 0.1

number_of_threads = 8

winners = []


def load_properties(filepath, sep='=', comment_char='#'):
    """
    Read the file passed as parameter as a properties file.
    """
    props = {}
    with open(filepath, "rt") as f:
        for line in f:
            l = line.strip()
            if l and not l.startswith(comment_char):
                key_value = l.split(sep)
                key = key_value[0].strip()
                value = sep.join(key_value[1:]).strip().strip('"')
                props[key] = value
    return props


def task():
    winners.append(compare.play_games(path_to_binary, map_width, map_height,
                             [run_bot_command, run_bot_command, run_bot_command, run_bot_command], tries))


def generate_randomized_values():
    for botNumber in range(4):
        properties = load_properties("bot" + str(botNumber) + ".properties")

        f = open("bot" + str(botNumber) + ".properties", "w")
        f.write("OBSTACLE_AVOIDANCE_DISTANCE=" +
                str((float(properties["OBSTACLE_AVOIDANCE_DISTANCE"])) + random.uniform(-randomization, randomization)))
        f.write("\n")
        f.write("BOUNDING_FACTOR=" +
                str((float(properties["BOUNDING_FACTOR"])) + random.uniform(-randomization, randomization)))
        f.write("\n")
        f.write("POSITION_TEND_FACTOR=" +
                str((float(properties["POSITION_TEND_FACTOR"])) + random.uniform(-randomization, randomization)))
        f.write("\n")
        f.write("SEPERATION_FACTOR=" +
                str((float(properties["SEPERATION_FACTOR"])) + random.uniform(-randomization, randomization)))
        f.write("\n")
        f.write("REGROUPING_RADIUS=" +
                str((float(properties["REGROUPING_RADIUS"])) + random.uniform(-randomization, randomization)))
        f.write("\n")
        f.write("OBSTACLE_AVOIDANCE_FACTOR=" +
                str((float(properties["OBSTACLE_AVOIDANCE_FACTOR"])) + random.uniform(-randomization, randomization)))
        f.write("\n")
        f.write("SEPERATION_DISTANCE=" +
                str((float(properties["SEPERATION_DISTANCE"])) + random.uniform(-randomization, randomization)))
        f.write("\n")
        f.write("REGROUPING_FACTOR=" +
                str((float(properties["REGROUPING_FACTOR"])) + random.uniform(-randomization, randomization)))
        f.write("\n")
        f.close()

def most_common(lst):
    return max(set(lst), key=lst.count)

if __name__ == "__main__":

    training_round = 0

    while True:
        print("Launching training round: "+str(training_round))
        print("Running with "+str(number_of_threads)+" threads and training "+str(tries)+" rounds.")

        os.system("javac MyBot.java")

        generate_randomized_values();

        threads = []

        for thread_number in range(number_of_threads):
            threads.append(threading.Thread(target=task))

        for thread in threads:
            thread.start()

        for thread in threads:
            thread.join()


        chained_winners_as_strings = list(chain.from_iterable(winners))
        chained_winners_as_numbers = []
        for winner in chained_winners_as_strings:
            chained_winners_as_numbers.append(int(winner.replace(",", "").replace("#","")))

        leading_bot = most_common(chained_winners_as_numbers)

        for botNumber in range(4):
            if leading_bot != botNumber:
                copyfile("bot" + str(leading_bot) + ".properties", "bot" + str(botNumber) + ".properties")

        print("Bot "+str(leading_bot)+" won the competition. He will now overtake.")

        os.system("find . -name \*.hlt -delete; find . -name \*.log -delete; find . -name \*.class -delete")
