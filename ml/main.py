from preprocess import preprocess
from train import train


def run():
    X, y = preprocess()
    train(X, y)


run()
