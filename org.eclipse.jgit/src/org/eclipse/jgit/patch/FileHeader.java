import org.eclipse.jgit.lib.Constants;
	public FileHeader(final byte[] headerLines, EditList edits, PatchType type) {
	FileHeader(final byte[] b, final int offset) {
	 * ({@link org.eclipse.jgit.lib.Constants#CHARSET}) is assumed for both the
			if (cs == null)
				cs = Constants.CHARSET;
		for (final HunkHeader h : getHunks())
	private static boolean trySimpleConversion(final Charset[] charsetGuess) {
	private String[] extractFileLines(final Charset[] csGuess) {
			for (final HunkHeader h : getHunks())
				if (cs == null)
					cs = Constants.CHARSET;
	void addHunk(final HunkHeader h) {
	HunkHeader newHunkHeader(final int offset) {
		for (final HunkHeader hunk : hunks)
	int parseGitFileName(int ptr, final int end) {
					oldPath = decode(Constants.CHARSET, buf, aStart, sp - 1);
	int parseGitHeaders(int ptr, final int end) {
	void parseOldName(int ptr, final int eol) {
	void parseNewName(int ptr, final int eol) {
	void parseNewFileMode(int ptr, final int eol) {
	int parseTraditionalHeaders(int ptr, final int end) {
	private String parseName(final String expect, int ptr, final int end) {
			r = decode(Constants.CHARSET, buf, ptr, tab - 1);
	FileMode parseFileMode(int ptr, final int end) {
	void parseIndexLine(int ptr, final int end) {
	static int isHunkHdr(final byte[] buf, final int start, final int end) {