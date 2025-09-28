当我们 `fork barda` 仓库后可能会需要与上游仓库同步，最简单的方法是点击 `fork` 仓库主页的 `Sync fork` -> `Update branch`，但当使用`gitlab`等托管代码时，也可以使用 `git upstream` (本文以 `gitlab` 为例)：

#### 步骤一
使用`gitlab`同步 `barda` 的项目后，使用 `git clone` 将仓库克隆到本地，然后运行 `git remote -v` 查看本地的远程仓库：
   ```sh
   $ git remote -v
   origin  http://192.168.1.131/saas-opensource/barda.git (fetch)
   origin  http://192.168.1.131/saas-opensource/barda.git (push)
   ```
#### 步骤二
如果没有 `upstream`，添加上游仓库：
   ```sh
   git remote add upstream git@github.com:mousheng/barda.git
   ```
#### 步骤三
再次查看本地的远程仓库：
   ```sh
   $ git remote -v
   origin    http://192.168.1.131/saas-opensource/barda.git (fetch)
   origin    http://192.168.1.131/saas-opensource/barda.git (push)
   upstream  git@github.com:mousheng/barda.git (fetch)
   upstream  git@github.com:mousheng/barda.git (push)
   ```
#### 步骤四
运行 `git fetch upstream` 获取上游仓库的提交：
   ```sh
   $ git fetch upstream
   remote: Enumerating objects: 5, done.
   remote: Counting objects: 100% (5/5), done.
   remote: Total 3 (delta 0), reused 0 (delta 0), pack-reused 0
   Unpacking objects: 100% (3/3), done.
   From github.com:mousheng/barda
    * [new branch]      barda       -> upstream/barda
   ```
#### 步骤五
切换到准备与上游仓库同步的分支（本文为 `barda`）：
   ```sh
   $ git checkout barda
   ```
#### 步骤六
将上游仓库的 `commit` 合并到本地分支：
   ```sh
   $ git merge upstream/barda
   Updating 141fa1c..dfe3645
   Fast-forward
    README.md | 2 ++
    1 file changed, 2 insertions(+)
   ```
#### 步骤七
如果需要，将本地分支推送到 `fork` 仓库：
   ```sh
   $ git push origin barda
   Total 0 (delta 0), reused 0 (delta 0), pack-reused 0
   To http://192.168.1.131/saas-opensource/barda.git
      141fa1c..dfe3645  barda -> barda
   ```

完成！
