#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Mon Sep  9 08:43:41 2019

@author: meiying
"""

import pandas as pd
import matplotlib.pyplot as plt
from sklearn.preprocessing import StandardScaler
from sklearn.cluster import KMeans
from sklearn.metrics import silhouette_score
import seaborn as sns
from sklearn.externals.joblib import load
import numpy as np

data = pd.read_excel('FoodDatabase v1.8.xlsx', sheet_name='AllFood-FoodType')
data = data[(data['Cuisine'].isin(['chinese', 'malay', 'indian', 'western']) & (data['Opta Type'] == 'Main') & (data['Analysis'] == 1))]
data = data.drop(['Analysis', 'MealPanner', 'HasBeef', 'IsCaffeinated', 'Multiplier', 'Carbohydrates %', 'Protein %', 'Fats %'], axis=1)
data['weight'] = data['Per Serving Household Measure'].str.extract('.*?(\(([0-9]*?) g\))')[1].astype('float')
data.describe()
data['pct_protein'] = data['Protein (g)'] * 4 / data['Energy'] * 100
data['pct_fat'] = data['Fats (g)'] * 9 / data['Energy'] * 100
data['pct_carb'] = data['Carbohydrates (g)'] * 4 / data['Energy'] * 100

sum_corr = data.corr().sum().sort_values(ascending=True).index.values
corr = data[sum_corr].corr()

plt.figure(figsize=(13, 8))
sns.heatmap(corr, annot=True, cmap='Greens');

data_new = data[['pct_protein', 'pct_fat', 'pct_carb', 'Sugar (g)', 'Dietary Fibre (g)', 'Sodium (mg)', 'Energy']]
data_new.dropna(axis=0, inplace=True)
data_new_std = StandardScaler().fit_transform(data_new)

sse = {}
silhouette_avg = {}

for k in range(2,9):
    kmeans = KMeans(n_clusters = k, random_state = 0)
    kmeans.fit(data_new_std)
    sse[k] = kmeans.inertia_
    cluster_labels = kmeans.labels_
    silhouette_avg[k] = silhouette_score(data_new_std, cluster_labels)
    print("For n_clusters =", k,
          "The average silhouette_score is :", silhouette_avg[k])

plt.figure()
#plt.title('The Elbow Method')
plt.xlabel('No. of clusters')
plt.ylabel('Sum of square error')
sns.pointplot(list(sse.keys()), list(sse.values()))
plt.show()    

best_k = [i for i,j in silhouette_avg.items() if j == max(list(silhouette_avg.values()))].pop()    
print("The average silhouette score is highest when there are " + str(best_k) + " clusters.")


kmeans = KMeans(n_clusters = 5, random_state = 0)
kmeans.fit(data_new_std)
cluster_labels = kmeans.labels_
data_new_k = data_new.assign(cluster=cluster_labels, cuisine=data['Cuisine'])
grouped = data_new_k.groupby(['cluster']).agg({
        'pct_protein': 'mean',
        'pct_fat': 'mean',
        'pct_carb': 'mean',
        'Sugar (g)': 'mean',
        'Dietary Fibre (g)': 'mean',
        'Sodium (mg)': 'mean',
        'Energy': 'mean'}).round(2)
    
count = data_new_k.groupby(['cluster']).count()

from scipy.stats import sem, t
confidence = 0.95    
grouped_sem = data_new_k.groupby(['cluster']).sem() * t.ppf((1 + confidence) / 2, count - 1)

#------------------------------------------------------------------------------

pct_protein = list(grouped['pct_protein'])
pct_fat = list(grouped['pct_fat'])
pct_carb = list(grouped['pct_carb'])

protein_sem = list(grouped_sem['pct_protein'])
fat_sem = list(grouped_sem['pct_fat'])
carb_sem = list(grouped_sem['pct_carb'])


components_dict = {
        'energy':[grouped['Energy'], grouped_sem['Energy'], 'Energy (kcal)'],
        'sugar':[grouped['Sugar (g)'], grouped_sem['Sugar (g)'], 'Amount of sugar (g)'],
        'dietary_fibre':[grouped['Dietary Fibre (g)'], grouped_sem['Sugar (g)'], 'Amount of dietary fibre (g)'],
        'sodium':[grouped['Sodium (mg)'], grouped_sem['Sodium (mg)'], 'Sodium (mg)']
        }

barwidth = 0.25
 
# set height of bar
barwidth = 0.25
cap = 5

# Set position of bar on X axis
r1 = np.arange(len(pct_protein))
r2 = [x + barwidth for x in r1]
r3 = [x + barwidth for x in r2]
 
# Make the plot
plt.bar(r1, pct_protein, yerr=protein_sem, color='yellow', capsize=cap, width=barwidth, label='protein')
plt.bar([x + barwidth for x in r1], pct_fat, yerr=fat_sem, capsize=cap, color='orange', width=barwidth, label='fat')
plt.bar([x + barwidth for x in r2], pct_carb, yerr=carb_sem, capsize=cap, color='red', width=barwidth, label='carbohydrates')
 
# Add xticks on the middle of the group bars
plt.xlabel('Cluster')
plt.ylabel('Percentage')
plt.xticks([r + barwidth for r in range(len(pct_protein))], r1 + 1)
 
# Create legend & Show graphic
plt.legend(loc='upper right', bbox_to_anchor=(1.38, 1))
plt.show()

#------------------------------------------------------------------------------

components = list(components_dict.keys())
new_r1 = r1 * 0.5

for i in range(len(components_dict)):
    plt.subplot(2, 2, i+1)
    plt.bar(new_r1, list(components_dict[components[i]][0]), yerr=list(components_dict[components[i]][1]), capsize=cap, color='orange', width=barwidth)
    plt.xlabel('Cluster')
    plt.ylabel(components_dict[components[i]][2])
    plt.xticks(new_r1, r1+1)
    plt.tight_layout()

import matplotlib.patches as mpatches

# data
cuisine_clusters_no = pd.pivot_table(data_new_k, index='cuisine', columns='cluster', aggfunc=len).iloc[:,0:5]
cuisine_clusters_no.fillna(0, inplace=True)
cuisine_clusters = cuisine_clusters_no.div(cuisine_clusters_no.sum(axis=1), axis=0) * 100
new_index = ['chinese', 'malay', 'indian', 'western']
cuisine_clusters = cuisine_clusters.reindex(index=new_index)
cluster1 = list(cuisine_clusters.iloc[:,0].astype(int))
cluster2 = list(cuisine_clusters.iloc[:,1].astype(int))
cluster3 = list(cuisine_clusters.iloc[:,2].astype(int))
cluster4 = list(cuisine_clusters.iloc[:,3].astype(int))
cluster5 = list(cuisine_clusters.iloc[:,4].astype(int))
cluster_high_fat_sodium_sugar = list(cuisine_clusters.iloc[:,2].astype(int))
x = np.arange(len(cluster1))

# plot
new_x = x * 0.5
plt.figure(figsize=(5, 3))
plt.bar(new_x, cluster1, width=barwidth, color='yellow')
plt.bar(new_x, cluster2, width=barwidth, color='red', bottom=cluster1)
plt.bar(new_x, cluster3, width=barwidth, color='orange', bottom=list(map(lambda w,x: w+x, cluster1, cluster2)))
plt.bar(new_x, cluster4, width=barwidth, color='green', bottom=list(map(lambda w,x,y: w+x+y, cluster1, cluster2, cluster3)))
plt.bar(new_x, cluster5, width=barwidth, color='blue', bottom=list(map(lambda w,x,y,z: w+x+y+z, cluster1, cluster2, cluster3, cluster4)))


# labels
plt.xticks(new_x, [i.capitalize() for i in new_index])
# plt.yticks(numpy.arange(10))
# plt.grid(axis='y')
plt.xlabel('Cuisine')
plt.ylabel('Percentage (%)')

# legend
green_patch = mpatches.Patch(color='green', label='high protein (cluster 4)')
blue_patch = mpatches.Patch(color='blue', label='high carbohydrates (cluster 5)')
yellow_patch = mpatches.Patch(color='yellow', label='high fat (cluster 1)')
orange_patch = mpatches.Patch(color='orange', label='high sugar and sodium (cluster 3)')
red_patch = mpatches.Patch(color='red', label='high fat, sugar, sodium and calories (cluster 2)')
plt.legend(handles=[yellow_patch, red_patch, orange_patch, green_patch, blue_patch], loc='best', bbox_to_anchor=(2, 1))
 
plt.show()
