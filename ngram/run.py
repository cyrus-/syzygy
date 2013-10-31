from ngram import *

def run_project(project):
	n_files = 10
	tokens_fmt = "%s.tokens%d"
	token_files = [tokens_fmt % (project, i) for i in xrange(n_files)]

	# n-fold cross-validation
	avg_prob = numpy.zeros((n_files,))
	for fold in xrange(n_files):
		training_files = [f for i, f in enumerate(token_files) if i != fold]
		test_file = token_files[fold]
		
		corpus = Corpus()
		for f in training_files:
			train_from_file(corpus, f)

		avg_prob[fold] = numpy.mean(evaluate_file(corpus, test_file))
	#print avg_prob
	return numpy.mean(avg_prob)

def train_from_file(corpus, file):
	lines = open(file).readlines()
	num_source_files = int(lines[0])
	for line in lines[num_source_files + 1:]:
		tokens = line.split(' ')
		for i in xrange(len(tokens)-2):
			corpus.incr_count(tokens[i:(i+3)])

def evaluate_file(corpus, test_file):
	lines = open(test_file).readlines()
	num_source_files = int(lines[0])
	prob = numpy.zeros((len(lines) - num_source_files - 1,))
	for i, line in enumerate(lines[num_source_files + 1:]):
		tokens = line.split(' ')
		if len(tokens) < 3:
			continue
		#print tokens[0:2], tokens[2:], corpus.seq_prob(tokens[2:], tokens[0:2])
		prob[i] = corpus.seq_prob(tokens[2:], tokens[0:2])
		assert prob[i] >= 0 and prob[i] <= 1.0
		#print prob[i]
	return prob

base = "../../stats/"
projects = [
#	"jfreechart",
	"ant",
#	"antlr",
#	"findbugs",
#	"batik",
#	"axion",
#	"AoIsrc292"
]

results = { }

for project in projects:
	results[project] = run_project(base + project)
