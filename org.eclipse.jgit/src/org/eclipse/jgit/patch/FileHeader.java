import org.eclipse.jgit.lib.Constants;
/** Patch header describing an action for a single file path. */
	public FileHeader(final byte[] headerLines, EditList edits, PatchType type) {
	FileHeader(final byte[] b, final int offset) {
	/** @return the byte array holding this file's patch script. */
	/** @return offset the start of this file's script in {@link #getBuffer()}. */
	/** @return offset one past the end of the file script. */
	 * The default character encoding ({@link Constants#CHARSET}) is assumed for
	 * both the old and new files.
			if (cs == null)
				cs = Constants.CHARSET;
		for (final HunkHeader h : getHunks())
	private static boolean trySimpleConversion(final Charset[] charsetGuess) {
	private String[] extractFileLines(final Charset[] csGuess) {
			for (final HunkHeader h : getHunks())
				if (cs == null)
					cs = Constants.CHARSET;
	/** @return style of patch used to modify this file */
	/** @return true if this patch modifies metadata about a file */
	/** @return hunks altering this file; in order of appearance in patch */
	void addHunk(final HunkHeader h) {
	HunkHeader newHunkHeader(final int offset) {
	/** @return if a {@link PatchType#GIT_BINARY}, the new-image delta/literal */
	/** @return if a {@link PatchType#GIT_BINARY}, the old-image delta/literal */
	/** @return a list describing the content edits performed on this file. */
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