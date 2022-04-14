import io
import numpy as np
import matplotlib.pyplot as plt
from sklearn.linear_model import LinearRegression
from sklearn.feature_extraction.text import TfidfTransformer

def recommendation(artist_rate_data,score):
    # X = np.array([[147, 150, 153, 158, 163, 165, 168, 170, 173, 175, 178, 180, 183]]).T
    # y = np.array([[ 49, 50, 51,  54, 58, 59, 60, 62, 63, 64, 66, 67, 68]]).T

    # artists_rate = np.array([[1,0,1,1,1],[0,0,1,1,0],[1,0,1,0,0],[0,0,1,0,1]])
    artists_rate = np.array(artist_rate_data)
    y = np.array(score)
    # y = np.array([3,0,5,0])
    #tfidf
    transformer = TfidfTransformer(smooth_idf=True, norm ='l2')
    tfidf = transformer.fit_transform(artists_rate.tolist()).toarray()

    clf = LinearRegression(fit_intercept  = True)
    clf.fit(tfidf, y)
    W = clf.coef_
    b = clf.intercept_

    Yhat = tfidf.dot(W) + b

    return Yhat.tolist()

def plot(array1,array2):
    x = np.sum(array1)

    # y = a * x + b
    y = np.sum(array2)

    # create a linear regression model
    model = LinearRegression()
    model.fit(x, y)

    # predict y from the data where the x is predicted from the x
    x_pred = np.linspace(0, 11, 100)
    y_pred = model.predict(x_pred[:, np.newaxis])

    # plot the results
    plt.figure(figsize =(3, 5))
    ax = plt.axes()
    ax.scatter(x, y)

    ax.plot(x_pred, y_pred)
    ax.set_xlabel('predictors')
    ax.set_ylabel('criterion')
    ax.axis('tight')

    f = io.BytesIO()
    plt.savefig(f, format="png")
    return f.getvalue()
