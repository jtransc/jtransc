var nodeJs = typeof process != 'undefined';

var BA_EXISTS    = 0x01;
var BA_REGULAR   = 0x02;
var BA_DIRECTORY = 0x04;
var BA_HIDDEN    = 0x08;
var ACCESS_EXECUTE = 0x01;
var ACCESS_WRITE   = 0x02;
var ACCESS_READ    = 0x04;

var IO = function() {
};

IO.getCwd = function() { return (nodeJs) ? process.cwd() : '/assets'; };

IO.getBooleanAttributes = function() {
	var out = 0;
	try {
		var stat = fs.lstatSync(path);
		out |= BA_EXISTS;
		if (stat.isFile()) out |= BA_REGULAR;
		if (stat.isDirectory()) out |= BA_DIRECTORY;
	} catch (e) {
	}
	return out;
};
