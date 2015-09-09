#!/bin/sh

bin=$JDBC_IMPORTER_HOME/bin
lib=$JDBC_IMPORTER_HOME/lib

echo '
{
    "type" : "jdbc",
    "jdbc" : {
        "url" : "jdbc:mysql://193.10.66.125:3306/?useUnicode=true&characterEncoding=UTF-8",
	"schedule" : "0/5 0-59 0-23 ? * *",
        "user" : "kthfs",
        "password" : "kthfs",
        "locale" : "en_US",
        "sql" : [
		{
		"statement": "INSERT INTO hopsworks.meta_inodes_ops_children_deleted (inodeid, parentid, processed) (SELECT hops.hdfs_metadata_log.inode_id, hops.hdfs_metadata_log.dataset_id, 0 FROM hops.hdfs_metadata_log LIMIT 100)"
		},

		{
		"statement": "SELECT composite._id, composite._parent, composite.name, composite.inode_id, composite.dataset_id as parentid, composite.operation, metadata.EXTENDED_METADATA FROM (SELECT DISTINCT ops.inode_id as _id, ops.dataset_id as _parent, inodeinn.name as name, ops.* FROM hops.hdfs_metadata_log ops,   (SELECT outt.id as nodeinn_id, outt.parent_id as _parent, outt.* FROM hops.hdfs_inodes outt, (SELECT i.id as parentid FROM hops.hdfs_inodes i, (SELECT inn.id AS rootid FROM hops.hdfs_inodes inn WHERE inn.parent_id = 1) AS temp WHERE i.parent_id = temp.rootid) AS parent WHERE outt.parent_id = parent.parentid) as inodeinn WHERE ops.operation = 0 AND ops.inode_id = inodeinn.nodeinn_id AND ops.inode_id IN (SELECT inodeid FROM hopsworks.meta_inodes_ops_children_deleted) LIMIT 100)as composite LEFT JOIN (SELECT mtt.inodeid, GROUP_CONCAT( md.data SEPARATOR \"|\" ) AS EXTENDED_METADATA FROM hopsworks.meta_tuple_to_file mtt, hopsworks.meta_data md WHERE mtt.tupleid = md.tupleid GROUP BY (mtt.inodeid) LIMIT 0 , 30) as metadata ON metadata.inodeid = composite._id ORDER BY composite.logical_time ASC"
		},

		{
		"statement": "SELECT ops.inode_id as _id, ops.dataset_id as _parent, ops.* FROM hops.hdfs_metadata_log ops WHERE ops.operation = 1 AND ops.inode_id IN (SELECT inode_id FROM hopsworks.meta_inodes_ops_children_deleted WHERE processed = 0)"
		},

		{
		"statement" : "UPDATE hopsworks.meta_inodes_ops_children_deleted m, (SELECT p.id AS id FROM hops.hdfs_inodes p, (SELECT inn.id AS rootid FROM hops.hdfs_inodes inn WHERE inn.parent_id = 1) AS root WHERE p.parent_id = root.rootid) AS parent SET m.processed = 1 WHERE m.parentid = parent.id AND m.inodeid IN (SELECT inode_id FROM hops.hdfs_metadata_log)"
		},

		{
		"statement" : "DELETE FROM hopsworks.meta_inodes_ops_children_deleted"
		},

		{
		"statement" : "UPDATE hops.hdfs_metadata_log set logical_time = -1 WHERE inode_id NOT IN (SELECT inodeid FROM hopsworks.meta_inodes_ops_children_deleted)"
		},

		{
		"statement" : "DELETE FROM hops.hdfs_metadata_log WHERE logical_time = -1"
		}

        ],
        "index" : "project",
	"type" : "child"
    }
}
'  | java \
              -cp "${lib}/*" \
              -Dlog4j.configurationFile=${bin}/log4j2.xml \
              org.xbib.tools.Runner \
              org.xbib.tools.JDBCImporter
