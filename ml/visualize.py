import matplotlib.pyplot as plt


def line(data, title, ylabel):
    for d in data:
        plt.plot(d.get('value'), label=d.get('label'))
    plt.title(title)
    plt.ylabel(ylabel)
    plt.xlabel('Num. of epochs')
    plt.legend(loc='upper right')
    plt.show()
