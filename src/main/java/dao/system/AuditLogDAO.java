             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, log.getEmpId());
            ps.setString(2, log.getActionName());
            ps.setString(3, log.getTableName());
            ps.setInt(4, log.getRecordId());
            ps.setString(5, log.getOldData());
            ps.setString(6, log.getNewData());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
