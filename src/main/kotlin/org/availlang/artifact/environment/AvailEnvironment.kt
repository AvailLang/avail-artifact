package org.availlang.artifact.environment

import java.io.File

/**
 * Contains state and behavior for managing the Avail environment on a computer.
 *
 * @author Richard Arriaga
 */
@Suppress("unused")
object AvailEnvironment
{
	/**
	 * The Avail home directory inside the user's home directory.
	 */
	@Suppress("MemberVisibilityCanBePrivate")
	val availHome: String get() =
		"${System.getProperty("user.home")}/.avail"

	/**
	 * The repositories directory inside the [Avail home directory][availHome]
	 * where the repositories, the persistent Avail indexed files of compiled
	 * modules, are stored.
	 */
	@Suppress("MemberVisibilityCanBePrivate")
	val availHomeRepos: String get() =
		"$availHome/repositories"

	/**
	 * The libraries directory inside the [Avail home directory][availHome]
	 * where imported Avail libraries are stored.
	 */
	@Suppress("MemberVisibilityCanBePrivate")
	val availHomeLibs: String get() =
		"$availHome/libraries"

	/**
	 * Add the
	 *   1. [`.avail` home directory][availHomeLibs]
	 *   2. [`.avail/repositories` repositories directory][availHomeRepos]
	 *   3. [`.avail/libraries` libraries directory][availHomeLibs]
	 *
	 * in the user's home directory if it doesn't already exist. Also adds the ;[]
	 */
	@Suppress("unused")
	fun optionallyCreateAvailUserHome ()
	{
		val availRepos = File(availHomeRepos)
		if (!availRepos.exists())
		{
			availRepos.mkdirs()
		}
		val availLibs = File(availHomeLibs)
		if (!availLibs.exists())
		{
			availLibs.mkdirs()
		}
	}

	/**
	 * Answer the root directory path of the Avail project from the provided
	 * string argument array. If the arguments are empty, presume this
	 *
	 */
	@Suppress("unused")
	fun getProjectRootDirectory (args: Array<String>): String =
		when
		{
			args.size == 1 ->
			{
				File(args[0]).apply {
					if (!isDirectory)
					{
						throw RuntimeException("")
					}
				}.absolutePath
			}
			args.isEmpty() ->
			{
				File("").absoluteFile.parent
			}
			else ->
				throw RuntimeException("")
		}
}
