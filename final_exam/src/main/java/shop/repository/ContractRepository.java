package shop.repository;

import shop.model.Contract;
import shop.model.Menu;

import java.util.List;

import org.springframework.data.domain.Page;

public interface ContractRepository {
	List<Contract> getAllContracts();

	List<Contract> findAll();

	List<Contract> getContractsByUserId(int usrId);

	List<Menu> findMenusByContractId(int contractId);

	List<Contract> getContractsExpiringSoon();

	List<Contract> findActiveContracts();

	Contract getContractById(int id);

	Contract findById(int id);

	void addContract(Contract contract);

	void updateContract(Contract contract);

	void deleteContract(int id);

	void updateContractStatus(int contractId, int status);

	void save(Contract contract);
	List<Contract> searchContracts(String keyword, int offset, int limit);

	int countContracts(String keyword);

}
