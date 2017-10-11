#!/bin/bash

[ -e release.revs.sh ] && source release.revs.sh

# the repos we want to check out, must setup variables below
# NOTE: poky must remain first
REPOS="poky metaoe metaintel metavirt"

POKY_URI="git://git.yoctoproject.org/poky.git"
POKY_PATH="poky"
POKY_REV="${POKY_REV-refs/remotes/origin/pyro}"

METAOE_URI="git://git.openembedded.org/meta-openembedded.git"
METAOE_PATH="poky/meta-openembedded"
METAOE_REV="${METAOE_REV-refs/remotes/origin/pyro}"

METAINTEL_URI="git://git.yoctoproject.org/meta-intel.git"
METAINTEL_PATH="poky/meta-intel"
METAINTEL_REV="${METAINTEL_REV-refs/remotes/origin/pyro}"

METAVIRT_URI="git://git.yoctoproject.org/meta-virtualization.git"
METAVIRT_PATH="poky/meta-virtualization"
METAVIRT_REV="${METAVIRT_REV-refs/remotes/origin/pyro}"

METAVIRTUALIZATIONINTEL_URI="meta-virtualization-intel"
METAVIRTUALIZATIONINTEL_PATH="poky/meta-virtualization-intel"

die() {
	echo "$*" >&2
	exit 1
}

update_repo() {
	uri=$1
	path=$2
	rev=$3

	# check if we already have it checked out, if so we just want to update
	if [[ -d ${path} ]]; then
		pushd ${path} > /dev/null
		echo "Updating '${path}'"
		git remote set-url origin "${uri}"
		git fetch origin || die "unable to fetch ${uri}"
	else
		echo "Cloning '${path}'"
		git clone ${uri} ${path} || die "unable to clone ${uri}"
		pushd ${path} > /dev/null
	fi

	# The reset steps are taken from Jenkins

	# Reset
	# * drop -d from clean to not nuke build/tmp
	# * add -e to not clear out bitbake bits
	git reset --hard || die "failed reset"
	git clean -fx -e bitbake -e meta/lib/oe || die "failed clean"

	# Call the branch what we're basing it on, otherwise use default
	# if the revision was not a branch.
	branch=$(basename ${rev})
	[[ "${branch}" == "${rev}" ]] && branch="default"

	# Create 'default' branch
	git update-ref refs/heads/${branch} ${rev} || \
		die "unable to get ${rev} of ${uri}"
	git config branch.${branch}.remote origin || die "failed config remote"
	git config branch.${branch}.merge ${rev} || die "failed config merge"
	git symbolic-ref HEAD refs/heads/${branch} || die "failed symbolic-ref"
	git reset --hard || die "failed reset"
	popd > /dev/null
	echo "Updated '${path}' to '${rev}'"
}

# For each repo, do the work
for repo in ${REPOS}; do
	# upper case the name
	repo=$(echo ${repo} | tr '[:lower:]' '[:upper:]')

	# expand variables
	expand_uri="${repo}_URI"
	expand_path="${repo}_PATH"
	expand_rev="${repo}_REV"
	repo_uri=${!expand_uri}
	repo_path=${!expand_path}
	repo_rev=${!expand_rev}

	# check that we've got data
	[[ -z ${repo_uri} ]] && die "No revision defined in ${expand_uri}"
	[[ -z ${repo_path} ]] && die "No revision defined in ${expand_path}"
	[[ -z ${repo_rev} ]] && die "No revision defined in ${expand_rev}"

	# now fetch/clone/update repo
	update_repo "${repo_uri}" "${repo_path}" "${repo_rev}"

done

rm -rf "${METAVIRTUALIZATIONINTEL_PATH}" || die "unable to clear old ${METAVIRTUALIZATIONINTEL_PATH}"
ln -sf "../${METAVIRTUALIZATIONINTEL_URI}" "${METAVIRTUALIZATIONINTEL_PATH}" || \
	die "unable to symlink ${METAVIRTUALIZATIONINTEL_PATH}"

exit 0
