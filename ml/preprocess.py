import pandas as pd
import tensorflow as tf
import numpy as np
from sklearn.preprocessing import MinMaxScaler


def get_real_data():
    data1 = pd.DataFrame(pd.read_csv('./data/real.csv'))
    data2 = pd.DataFrame(pd.read_csv('./data/real2.csv'))
    data = data1.append(data2, ignore_index=True)
    data.drop(['label', 'left_shoulder_x', 'left_shoulder_y',
               'right_shoulder_x', 'right_shoulder_y'], axis=1, inplace=True)
    return data


def get_unreal_data():
    data1 = pd.DataFrame(pd.read_csv('./data/unreal.csv'))
    data2 = pd.DataFrame(pd.read_csv('./data/unreal2.csv'))
    data = data1.append(data2, ignore_index=True)
    data.drop(['left_shoulder_x', 'left_shoulder_y',
               'right_shoulder_x', 'right_shoulder_y'], axis=1, inplace=True)
    data['label'] = data['label'] - 1
    return data


def get_train_label_data(data: pd.DataFrame):
    scaler = MinMaxScaler()
    X = data.iloc[:, 0:56]
    X = scaler.fit_transform(X).astype('float32')
    y = tf.one_hot(data.iloc[:, 56], 3)
    return (X, y)


def preprocess():
    real_data = get_real_data()
    unreal_data = get_unreal_data()
    data = real_data.merge(unreal_data, how='inner', on='id', suffixes=(
        '_real', '_unreal')).drop(columns=['id'])
    return get_train_label_data(data)
