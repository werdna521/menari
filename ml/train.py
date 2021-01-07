from tensorflow.keras import Input, Model
from tensorflow.keras.layers import Dense, BatchNormalization
from visualize import line


def define_model():
    inputs = Input(shape=(56,))
    hidden1 = Dense(32, activation='relu', kernel_regularizer='l2')(inputs)
    norm1 = BatchNormalization()(hidden1)
    hidden2 = Dense(16, activation='relu', kernel_regularizer='l2')(norm1)
    norm2 = BatchNormalization()(hidden2)
    outputs = Dense(3, activation='softmax')(norm2)
    return Model(inputs=inputs, outputs=outputs)


def plot_history(history):
    acc = history.history['accuracy']
    val_acc = history.history['val_accuracy']
    loss = history.history['loss']
    val_loss = history.history['val_loss']

    line([{'value': acc, 'label': 'Accuracy'}, {'value': val_acc,
                                                'label': 'Validation Accuracy'}], 'Accuracy', 'accuracy')
    line([{'value': loss, 'label': 'Loss'}, {'value': val_loss,
                                             'label': 'Validation Loss'}], 'Loss', 'loss')


def train(X, y):
    model = define_model()
    model.summary()
    model.compile(optimizer='rmsprop',
                  loss='categorical_crossentropy', metrics=['accuracy'])
    history = model.fit(X, y, epochs=500, validation_split=.2)
    plot_history(history)
