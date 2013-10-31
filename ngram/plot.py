projects = ['AVERAGE', 'ant', 'antlr', 'axion', 'batik', 'findbugs', 'jfreechart']
results_ng = [0.20569944487076239, 
	0.20387159108666789, 0.20998040562069314, 0.20504841415171504, 0.22110264349229758, 0.20049990414342958, 0.19369371072977096]
results_ours = [0.24709333333333336,
	0.23239, 0.31617, 0.25602, 0.20795, 0.19624, 0.27379]

import numpy as np
import matplotlib.pyplot as plt

N = len(projects)
ind = np.arange(N)
width = 0.35

fig, ax = plt.subplots()
#rects1m = ax.bar(ind[0], results_ng[0], width, color='')
rects1m = ax.bar([width*2+width], results_ng[0], width, color='#ff7400')
rects2m = ax.bar([width*2], results_ours[0], width, color='#008500')
rects1 = ax.bar(2*width + ind[1:]+width, results_ng[1:], width, color='#ff9640')
rects2 = ax.bar(2*width + ind[1:], results_ours[1:], width, color='#00cc00')

ax.set_xlabel('Project')
ax.set_ylabel('Average $\mathbf{P}(e)$')
ax.set_xticks(2*width + ind + width)
ax.set_xticklabels(projects)
ax.legend((rects2[0],rects1[0]), ('our model','3-gram'))

plt.savefig('accuracy.eps')
